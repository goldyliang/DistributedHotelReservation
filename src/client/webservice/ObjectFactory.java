
package client.webservice;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the client.webservice package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _LoginAsManagerResponse_QNAME = new QName("http://serverws/", "loginAsManagerResponse");
    private final static QName _TransferRoomResponse_QNAME = new QName("http://serverws/", "transferRoomResponse");
    private final static QName _GeneralReturn_QNAME = new QName("http://serverws/", "generalReturn");
    private final static QName _GetServiceReport_QNAME = new QName("http://serverws/", "getServiceReport");
    private final static QName _ManagerReturn_QNAME = new QName("http://serverws/", "managerReturn");
    private final static QName _HotelProfileElem_QNAME = new QName("http://serverws/", "HotelProfileElem");
    private final static QName _GetReserveRecordsResponse_QNAME = new QName("http://serverws/", "getReserveRecordsResponse");
    private final static QName _GetStatusReportResponse_QNAME = new QName("http://serverws/", "getStatusReportResponse");
    private final static QName _ReserveRoom_QNAME = new QName("http://serverws/", "reserveRoom");
    private final static QName _GetProfileResponse_QNAME = new QName("http://serverws/", "getProfileResponse");
    private final static QName _SimpleDate_QNAME = new QName("http://serverws/", "simpleDate");
    private final static QName _CancelRoom_QNAME = new QName("http://serverws/", "cancelRoom");
    private final static QName _GetStatusReport_QNAME = new QName("http://serverws/", "getStatusReport");
    private final static QName _CancelRoomResponse_QNAME = new QName("http://serverws/", "cancelRoomResponse");
    private final static QName _GetReserveRecords_QNAME = new QName("http://serverws/", "getReserveRecords");
    private final static QName _CheckAvailability_QNAME = new QName("http://serverws/", "checkAvailability");
    private final static QName _GetServiceReportResponse_QNAME = new QName("http://serverws/", "getServiceReportResponse");
    private final static QName _ReserveRoomResponse_QNAME = new QName("http://serverws/", "reserveRoomResponse");
    private final static QName _TransferRoom_QNAME = new QName("http://serverws/", "transferRoom");
    private final static QName _GetProfile_QNAME = new QName("http://serverws/", "getProfile");
    private final static QName _CheckAvailabilityResponse_QNAME = new QName("http://serverws/", "checkAvailabilityResponse");
    private final static QName _LoginAsManager_QNAME = new QName("http://serverws/", "loginAsManager");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: client.webservice
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link HotelProfile }
     * 
     */
    public HotelProfile createHotelProfile() {
        return new HotelProfile();
    }

    /**
     * Create an instance of {@link HotelProfile.Rates }
     * 
     */
    public HotelProfile.Rates createHotelProfileRates() {
        return new HotelProfile.Rates();
    }

    /**
     * Create an instance of {@link HotelProfile.TotalRooms }
     * 
     */
    public HotelProfile.TotalRooms createHotelProfileTotalRooms() {
        return new HotelProfile.TotalRooms();
    }

    /**
     * Create an instance of {@link LoginAsManagerResponse }
     * 
     */
    public LoginAsManagerResponse createLoginAsManagerResponse() {
        return new LoginAsManagerResponse();
    }

    /**
     * Create an instance of {@link GetStatusReportResponse }
     * 
     */
    public GetStatusReportResponse createGetStatusReportResponse() {
        return new GetStatusReportResponse();
    }

    /**
     * Create an instance of {@link GetReserveRecordsResponse }
     * 
     */
    public GetReserveRecordsResponse createGetReserveRecordsResponse() {
        return new GetReserveRecordsResponse();
    }

    /**
     * Create an instance of {@link ManagerReturn }
     * 
     */
    public ManagerReturn createManagerReturn() {
        return new ManagerReturn();
    }

    /**
     * Create an instance of {@link GetServiceReport }
     * 
     */
    public GetServiceReport createGetServiceReport() {
        return new GetServiceReport();
    }

    /**
     * Create an instance of {@link TransferRoomResponse }
     * 
     */
    public TransferRoomResponse createTransferRoomResponse() {
        return new TransferRoomResponse();
    }

    /**
     * Create an instance of {@link GeneralReturn }
     * 
     */
    public GeneralReturn createGeneralReturn() {
        return new GeneralReturn();
    }

    /**
     * Create an instance of {@link SimpleDate }
     * 
     */
    public SimpleDate createSimpleDate() {
        return new SimpleDate();
    }

    /**
     * Create an instance of {@link GetProfileResponse }
     * 
     */
    public GetProfileResponse createGetProfileResponse() {
        return new GetProfileResponse();
    }

    /**
     * Create an instance of {@link ReserveRoom }
     * 
     */
    public ReserveRoom createReserveRoom() {
        return new ReserveRoom();
    }

    /**
     * Create an instance of {@link LoginAsManager }
     * 
     */
    public LoginAsManager createLoginAsManager() {
        return new LoginAsManager();
    }

    /**
     * Create an instance of {@link CheckAvailabilityResponse }
     * 
     */
    public CheckAvailabilityResponse createCheckAvailabilityResponse() {
        return new CheckAvailabilityResponse();
    }

    /**
     * Create an instance of {@link GetProfile }
     * 
     */
    public GetProfile createGetProfile() {
        return new GetProfile();
    }

    /**
     * Create an instance of {@link TransferRoom }
     * 
     */
    public TransferRoom createTransferRoom() {
        return new TransferRoom();
    }

    /**
     * Create an instance of {@link ReserveRoomResponse }
     * 
     */
    public ReserveRoomResponse createReserveRoomResponse() {
        return new ReserveRoomResponse();
    }

    /**
     * Create an instance of {@link GetServiceReportResponse }
     * 
     */
    public GetServiceReportResponse createGetServiceReportResponse() {
        return new GetServiceReportResponse();
    }

    /**
     * Create an instance of {@link CheckAvailability }
     * 
     */
    public CheckAvailability createCheckAvailability() {
        return new CheckAvailability();
    }

    /**
     * Create an instance of {@link GetReserveRecords }
     * 
     */
    public GetReserveRecords createGetReserveRecords() {
        return new GetReserveRecords();
    }

    /**
     * Create an instance of {@link CancelRoomResponse }
     * 
     */
    public CancelRoomResponse createCancelRoomResponse() {
        return new CancelRoomResponse();
    }

    /**
     * Create an instance of {@link GetStatusReport }
     * 
     */
    public GetStatusReport createGetStatusReport() {
        return new GetStatusReport();
    }

    /**
     * Create an instance of {@link CancelRoom }
     * 
     */
    public CancelRoom createCancelRoom() {
        return new CancelRoom();
    }

    /**
     * Create an instance of {@link Record }
     * 
     */
    public Record createRecord() {
        return new Record();
    }

    /**
     * Create an instance of {@link Availability }
     * 
     */
    public Availability createAvailability() {
        return new Availability();
    }

    /**
     * Create an instance of {@link HotelProfile.Rates.Entry }
     * 
     */
    public HotelProfile.Rates.Entry createHotelProfileRatesEntry() {
        return new HotelProfile.Rates.Entry();
    }

    /**
     * Create an instance of {@link HotelProfile.TotalRooms.Entry }
     * 
     */
    public HotelProfile.TotalRooms.Entry createHotelProfileTotalRoomsEntry() {
        return new HotelProfile.TotalRooms.Entry();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LoginAsManagerResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://serverws/", name = "loginAsManagerResponse")
    public JAXBElement<LoginAsManagerResponse> createLoginAsManagerResponse(LoginAsManagerResponse value) {
        return new JAXBElement<LoginAsManagerResponse>(_LoginAsManagerResponse_QNAME, LoginAsManagerResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TransferRoomResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://serverws/", name = "transferRoomResponse")
    public JAXBElement<TransferRoomResponse> createTransferRoomResponse(TransferRoomResponse value) {
        return new JAXBElement<TransferRoomResponse>(_TransferRoomResponse_QNAME, TransferRoomResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GeneralReturn }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://serverws/", name = "generalReturn")
    public JAXBElement<GeneralReturn> createGeneralReturn(GeneralReturn value) {
        return new JAXBElement<GeneralReturn>(_GeneralReturn_QNAME, GeneralReturn.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetServiceReport }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://serverws/", name = "getServiceReport")
    public JAXBElement<GetServiceReport> createGetServiceReport(GetServiceReport value) {
        return new JAXBElement<GetServiceReport>(_GetServiceReport_QNAME, GetServiceReport.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ManagerReturn }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://serverws/", name = "managerReturn")
    public JAXBElement<ManagerReturn> createManagerReturn(ManagerReturn value) {
        return new JAXBElement<ManagerReturn>(_ManagerReturn_QNAME, ManagerReturn.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link HotelProfile }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://serverws/", name = "HotelProfileElem")
    public JAXBElement<HotelProfile> createHotelProfileElem(HotelProfile value) {
        return new JAXBElement<HotelProfile>(_HotelProfileElem_QNAME, HotelProfile.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetReserveRecordsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://serverws/", name = "getReserveRecordsResponse")
    public JAXBElement<GetReserveRecordsResponse> createGetReserveRecordsResponse(GetReserveRecordsResponse value) {
        return new JAXBElement<GetReserveRecordsResponse>(_GetReserveRecordsResponse_QNAME, GetReserveRecordsResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetStatusReportResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://serverws/", name = "getStatusReportResponse")
    public JAXBElement<GetStatusReportResponse> createGetStatusReportResponse(GetStatusReportResponse value) {
        return new JAXBElement<GetStatusReportResponse>(_GetStatusReportResponse_QNAME, GetStatusReportResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReserveRoom }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://serverws/", name = "reserveRoom")
    public JAXBElement<ReserveRoom> createReserveRoom(ReserveRoom value) {
        return new JAXBElement<ReserveRoom>(_ReserveRoom_QNAME, ReserveRoom.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetProfileResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://serverws/", name = "getProfileResponse")
    public JAXBElement<GetProfileResponse> createGetProfileResponse(GetProfileResponse value) {
        return new JAXBElement<GetProfileResponse>(_GetProfileResponse_QNAME, GetProfileResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SimpleDate }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://serverws/", name = "simpleDate")
    public JAXBElement<SimpleDate> createSimpleDate(SimpleDate value) {
        return new JAXBElement<SimpleDate>(_SimpleDate_QNAME, SimpleDate.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CancelRoom }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://serverws/", name = "cancelRoom")
    public JAXBElement<CancelRoom> createCancelRoom(CancelRoom value) {
        return new JAXBElement<CancelRoom>(_CancelRoom_QNAME, CancelRoom.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetStatusReport }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://serverws/", name = "getStatusReport")
    public JAXBElement<GetStatusReport> createGetStatusReport(GetStatusReport value) {
        return new JAXBElement<GetStatusReport>(_GetStatusReport_QNAME, GetStatusReport.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CancelRoomResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://serverws/", name = "cancelRoomResponse")
    public JAXBElement<CancelRoomResponse> createCancelRoomResponse(CancelRoomResponse value) {
        return new JAXBElement<CancelRoomResponse>(_CancelRoomResponse_QNAME, CancelRoomResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetReserveRecords }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://serverws/", name = "getReserveRecords")
    public JAXBElement<GetReserveRecords> createGetReserveRecords(GetReserveRecords value) {
        return new JAXBElement<GetReserveRecords>(_GetReserveRecords_QNAME, GetReserveRecords.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CheckAvailability }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://serverws/", name = "checkAvailability")
    public JAXBElement<CheckAvailability> createCheckAvailability(CheckAvailability value) {
        return new JAXBElement<CheckAvailability>(_CheckAvailability_QNAME, CheckAvailability.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetServiceReportResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://serverws/", name = "getServiceReportResponse")
    public JAXBElement<GetServiceReportResponse> createGetServiceReportResponse(GetServiceReportResponse value) {
        return new JAXBElement<GetServiceReportResponse>(_GetServiceReportResponse_QNAME, GetServiceReportResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReserveRoomResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://serverws/", name = "reserveRoomResponse")
    public JAXBElement<ReserveRoomResponse> createReserveRoomResponse(ReserveRoomResponse value) {
        return new JAXBElement<ReserveRoomResponse>(_ReserveRoomResponse_QNAME, ReserveRoomResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TransferRoom }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://serverws/", name = "transferRoom")
    public JAXBElement<TransferRoom> createTransferRoom(TransferRoom value) {
        return new JAXBElement<TransferRoom>(_TransferRoom_QNAME, TransferRoom.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetProfile }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://serverws/", name = "getProfile")
    public JAXBElement<GetProfile> createGetProfile(GetProfile value) {
        return new JAXBElement<GetProfile>(_GetProfile_QNAME, GetProfile.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CheckAvailabilityResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://serverws/", name = "checkAvailabilityResponse")
    public JAXBElement<CheckAvailabilityResponse> createCheckAvailabilityResponse(CheckAvailabilityResponse value) {
        return new JAXBElement<CheckAvailabilityResponse>(_CheckAvailabilityResponse_QNAME, CheckAvailabilityResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LoginAsManager }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://serverws/", name = "loginAsManager")
    public JAXBElement<LoginAsManager> createLoginAsManager(LoginAsManager value) {
        return new JAXBElement<LoginAsManager>(_LoginAsManager_QNAME, LoginAsManager.class, null, value);
    }

}
