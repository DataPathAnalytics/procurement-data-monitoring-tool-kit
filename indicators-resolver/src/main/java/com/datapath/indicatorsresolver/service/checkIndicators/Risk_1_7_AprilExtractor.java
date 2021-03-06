package com.datapath.indicatorsresolver.service.checkIndicators;

import com.datapath.indicatorsresolver.model.TenderDimensions;
import com.datapath.indicatorsresolver.model.TenderIndicator;
import com.datapath.persistence.entities.Indicator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.*;

import static com.datapath.indicatorsresolver.IndicatorConstants.UA_ZONE;
import static com.datapath.persistence.utils.DateUtils.toZonedDateTime;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;


@Service
@Slf4j
public class Risk_1_7_AprilExtractor extends BaseExtractor {

    private final String INDICATOR_CODE = "RISK-1-7";
    private final Integer DAYS_LIMIT = 5;

    private boolean indicatorsResolverAvailable;

    public Risk_1_7_AprilExtractor() {
        indicatorsResolverAvailable = true;
    }


    public void checkIndicator(ZonedDateTime dateTime) {
        try {
            indicatorsResolverAvailable = false;
            Indicator indicator = getIndicator(INDICATOR_CODE);
            if (indicator.isActive()) {
                checkIndicator(indicator, dateTime);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            indicatorsResolverAvailable = true;
        }
    }

    @Async
    @Scheduled(cron = "${risk-1-7.cron}")
    public void checkIndicator() {
        if (!indicatorsResolverAvailable) {
            log.info(String.format(INDICATOR_NOT_AVAILABLE_MESSAGE_FORMAT, INDICATOR_CODE));
            return;
        }
        try {
            indicatorsResolverAvailable = false;
            Indicator indicator = getIndicator(INDICATOR_CODE);
            if (indicator.isActive()) {
                ZonedDateTime dateTime = isNull(indicator.getLastCheckedDateCreated())
                        ? ZonedDateTime.now().minus(Period.ofYears(1)).withHour(0)
                        : indicator.getLastCheckedDateCreated();
                checkIndicator(indicator, dateTime);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        } finally {
            indicatorsResolverAvailable = true;
        }
    }


    private void checkIndicator(Indicator indicator, ZonedDateTime dateTime) {
        log.info("{} indicator started", INDICATOR_CODE);
        while (true) {
            log.info("Start finding tenders after [{}]", dateTime.toString());
            List<String> tenders = findTenders(
                    dateTime,
                    Arrays.asList(indicator.getProcedureStatuses()),
                    Arrays.asList(indicator.getProcedureTypes()),
                    Arrays.asList(indicator.getProcuringEntityKind()));

            log.info("Finish finding tenders. Found [{}] tenders", tenders.size());
            if (tenders.isEmpty()) {
                break;
            }

            Map<String, TenderDimensions> dimensionsMap = getTenderDimensionsWithIndicatorLastIteration(new HashSet<>(tenders), INDICATOR_CODE);

            Map<String, List<TenderIndicator>> tenderIndicatorsMap = checkIndicator(tenders, indicator);
            tenderIndicatorsMap.forEach((tenderId, tenderIndicators) -> {
                tenderIndicators.forEach(tenderIndicator -> tenderIndicator.setTenderDimensions(dimensionsMap.get(tenderId)));
                uploadIndicatorIfNotExists(tenderId, INDICATOR_CODE, tenderIndicators);

            });

            ZonedDateTime maxTenderDateCreated = getMaxTenderDateCreated(dimensionsMap, dateTime);
            indicator.setLastCheckedDateCreated(maxTenderDateCreated);
            indicatorRepository.save(indicator);

            dateTime = maxTenderDateCreated;
        }
        ZonedDateTime now = ZonedDateTime.now();
        indicator.setDateChecked(now);
        indicatorRepository.save(indicator);

        log.info("{} indicator finished", INDICATOR_CODE);
    }


    private Map<String, List<TenderIndicator>> checkIndicator(List<String> tenderIds, Indicator indicator) {
        Map<String, Map<Integer, List<String>>> resultMap = new HashMap<>();
        Map<String, List<TenderIndicator>> result = new HashMap<>();


        Map<String, Long> maxTendersIndicatorIteration = extractDataService
                .getMaxTenderIndicatorIteration(new HashSet<>(tenderIds), INDICATOR_CODE);

        Map<String, Map<String, Integer>> maxTendersLotIterationData = extractDataService
                .getMaxTendersLotIterationData(maxTendersIndicatorIteration, INDICATOR_CODE);


        List<Object[]> tenderLotdateDocComplaintCount = tenderRepository
                .getTenderLotDateNoSignatureDocComplaintCountByTenderIds(tenderIds.stream().collect(joining(",")));

        ZonedDateTime currentDate = ZonedDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT, UA_ZONE);
        ZonedDateTime dateOfCurrentDateMinusNWorkingDays = getDateOfDateMinusNWorkingDays(currentDate.minusDays(1), DAYS_LIMIT);

        tenderLotdateDocComplaintCount.forEach(tenderLotData -> {
            String tenderId = tenderLotData[0].toString();
            String lotId = tenderLotData[1].toString();

            log.info("Process tender {} lot {}", tenderId, lotId);

            int indicatorValue;
            try {
                Object awardIdObj = tenderLotData[2];
                Timestamp awardDateTimestamp = (Timestamp) tenderLotData[3];
                int docCount = Integer.parseInt(tenderLotData[4].toString());
                int complaintsCount = Integer.parseInt(tenderLotData[5].toString());

                if (maxTendersLotIterationData.get(tenderId).containsKey(lotId) && maxTendersLotIterationData.get(tenderId).get(lotId).equals(1)) {
                    indicatorValue = RISK;
                } else {
                    if (isNull(awardIdObj)) {
                        indicatorValue = NOT_RISK;
                    } else {
                        ZonedDateTime awardDate = toUaMidnight(toZonedDateTime(awardDateTimestamp));
                        indicatorValue = (!awardDate.isBefore(dateOfCurrentDateMinusNWorkingDays)
                                || docCount > 0 || complaintsCount > 0)
                                ? CONDITIONS_NOT_MET
                                : RISK;
                    }
                }
            } catch (Exception e) {
                logService.lotIndicatorFailed(INDICATOR_CODE, tenderId, lotId, e);
                indicatorValue = IMPOSSIBLE_TO_DETECT;
            }

            if (!resultMap.containsKey(tenderId)) {
                resultMap.put(tenderId, new HashMap<>());
            }
            if (!resultMap.get(tenderId).containsKey(indicatorValue)) {
                resultMap.get(tenderId).put(indicatorValue, new ArrayList<>());
            }
            resultMap.get(tenderId).get(indicatorValue).add(lotId);

        });

        resultMap.forEach((tenderOuterId, value) -> {
            TenderDimensions tenderDimensions = new TenderDimensions(tenderOuterId);
            value.forEach((indicatorValue, lots) -> {
                if (!result.containsKey(tenderOuterId)) result.put(tenderOuterId, new ArrayList<>());
                result.get(tenderOuterId).add(new TenderIndicator(tenderDimensions, indicator, indicatorValue, lots));
            });
        });

        return result;
    }
}
