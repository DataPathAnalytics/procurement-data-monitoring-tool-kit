package com.datapath.persistence.entities;

import com.datapath.persistence.converters.ZonedDateTimeConverter;
import com.datapath.persistence.type.StringArrayUserType;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "tender",
        indexes = {
                @Index(columnList = "outer_id", name = "tender_outer_id_idx"),
                @Index(columnList = "procuring_entity_id", name = "tender_procuring_entity_id_idx")
        })
@ToString(exclude = {"procuringEntity", "items", "tenderContracts", "awards", "data", "bids", "lots", "documents"})
@TypeDef(name = "array", typeClass = StringArrayUserType.class)
public class Tender {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "outer_id", unique = true)
    private String outerId;

    @Column(name = "tender_id")
    private String tenderId;

    @Column(name = "status")
    private String status;

    @Column(name = "source")
    private String source;

    @Column(name = "date_modified")
    @Convert(converter = ZonedDateTimeConverter.class)
    private ZonedDateTime dateModified;

    @Column(name = "date")
    @Convert(converter = ZonedDateTimeConverter.class)
    private ZonedDateTime date;

    @Column(name = "date_created")
    @Convert(converter = ZonedDateTimeConverter.class)
    private ZonedDateTime dateCreated;

    @Column(name = "procurement_method_type")
    private String procurementMethodType;

    @Column(name = "procurement_method")
    private String procurementMethod;

    @Column(name = "tv_procuring_entity")
    private String tvProcuringEntity;

    @Column(name = "tv_subject_of_procurement")
    private String tvSubjectOfProcurement;

    @Column(name = "tv_tender_cpv")
    private String tvTenderCPV;

    @Column(columnDefinition = "text[]", name = "tv_tender_cpv_list")
    @Type(type = "array")
    private String[] tvTenderCPVList;

    @Column(name = "cause")
    private String cause;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "currency")
    private String currency;

    @Column(name = "guarantee_amount")
    private Double guaranteeAmount;

    @Column(name = "guarantee_currency")
    private String guaranteeCurrency;

    @Column(name = "title")
    @Type(type = "org.hibernate.type.TextType")
    private String title;

    @Column(name = "procuring_entity_kind")
    private String procuringEntityKind;

    @Column(name = "start_date")
    @Convert(converter = ZonedDateTimeConverter.class)
    private ZonedDateTime startDate;

    @Column(name = "end_date")
    @Convert(converter = ZonedDateTimeConverter.class)
    private ZonedDateTime endDate;

    @Column(name = "enquiry_start_date")
    @Convert(converter = ZonedDateTimeConverter.class)
    private ZonedDateTime enquiryStartDate;

    @Column(name = "enquiry_end_date")
    @Convert(converter = ZonedDateTimeConverter.class)
    private ZonedDateTime enquiryEndDate;

    @Column(name = "award_start_date")
    @Convert(converter = ZonedDateTimeConverter.class)
    private ZonedDateTime awardStartDate;

    @Column(name = "award_end_date")
    @Convert(converter = ZonedDateTimeConverter.class)
    private ZonedDateTime awardEndDate;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "procuring_entity_id")
    private ProcuringEntity procuringEntity;

    @OneToMany(mappedBy = "tender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TenderItem> items;

    @OneToMany(mappedBy = "tender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TenderContract> tenderContracts;

    @OneToMany(mappedBy = "tender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Award> awards;

    @OneToOne(mappedBy = "tender", cascade = CascadeType.ALL, orphanRemoval = true)
    private TenderData data;

    @OneToMany(mappedBy = "tender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bid> bids;

    @OneToMany(mappedBy = "tender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lot> lots;

    @OneToMany(mappedBy = "tender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents;

    @OneToMany(mappedBy = "tender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EligibilityDocument> eligibilityDocuments;

    @OneToMany(mappedBy = "tender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FinancialDocument> financialDocuments;

    @OneToMany(mappedBy = "tender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;

    @OneToMany(mappedBy = "tender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Qualification> qualifications;

    public Tender() {
    }

    public Tender(String outerId, String tenderId, ZonedDateTime date, ZonedDateTime dateModified, String procurementMethodType, String status, ZonedDateTime dateCreated) {
        this.outerId = outerId;
        this.tenderId = tenderId;
        this.date = date;
        this.dateModified = dateModified;
        this.procurementMethodType = procurementMethodType;
        this.status = status;
        this.dateCreated = dateCreated;

    }
}