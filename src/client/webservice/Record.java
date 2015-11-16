
package client.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for record complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="record">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="resID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="guestID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="shortName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="roomType" type="{http://serverws/}roomType" minOccurs="0"/>
 *         &lt;element name="checkInDate" type="{http://serverws/}simpleDate" minOccurs="0"/>
 *         &lt;element name="checkOutDate" type="{http://serverws/}simpleDate" minOccurs="0"/>
 *         &lt;element name="rate" type="{http://www.w3.org/2001/XMLSchema}float"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "record", propOrder = {
    "resID",
    "guestID",
    "shortName",
    "roomType",
    "checkInDate",
    "checkOutDate",
    "rate"
})
public class Record {

    protected int resID;
    protected String guestID;
    protected String shortName;
    protected RoomType roomType;
    protected SimpleDate checkInDate;
    protected SimpleDate checkOutDate;
    protected float rate;

    /**
     * Gets the value of the resID property.
     * 
     */
    public int getResID() {
        return resID;
    }

    /**
     * Sets the value of the resID property.
     * 
     */
    public void setResID(int value) {
        this.resID = value;
    }

    /**
     * Gets the value of the guestID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGuestID() {
        return guestID;
    }

    /**
     * Sets the value of the guestID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGuestID(String value) {
        this.guestID = value;
    }

    /**
     * Gets the value of the shortName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * Sets the value of the shortName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShortName(String value) {
        this.shortName = value;
    }

    /**
     * Gets the value of the roomType property.
     * 
     * @return
     *     possible object is
     *     {@link RoomType }
     *     
     */
    public RoomType getRoomType() {
        return roomType;
    }

    /**
     * Sets the value of the roomType property.
     * 
     * @param value
     *     allowed object is
     *     {@link RoomType }
     *     
     */
    public void setRoomType(RoomType value) {
        this.roomType = value;
    }

    /**
     * Gets the value of the checkInDate property.
     * 
     * @return
     *     possible object is
     *     {@link SimpleDate }
     *     
     */
    public SimpleDate getCheckInDate() {
        return checkInDate;
    }

    /**
     * Sets the value of the checkInDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link SimpleDate }
     *     
     */
    public void setCheckInDate(SimpleDate value) {
        this.checkInDate = value;
    }

    /**
     * Gets the value of the checkOutDate property.
     * 
     * @return
     *     possible object is
     *     {@link SimpleDate }
     *     
     */
    public SimpleDate getCheckOutDate() {
        return checkOutDate;
    }

    /**
     * Sets the value of the checkOutDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link SimpleDate }
     *     
     */
    public void setCheckOutDate(SimpleDate value) {
        this.checkOutDate = value;
    }

    /**
     * Gets the value of the rate property.
     * 
     */
    public float getRate() {
        return rate;
    }

    /**
     * Sets the value of the rate property.
     * 
     */
    public void setRate(float value) {
        this.rate = value;
    }

}
