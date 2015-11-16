
package client.webservice;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for errorCode.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="errorCode">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="SUCCESS"/>
 *     &lt;enumeration value="HOTEL_NOT_FOUND"/>
 *     &lt;enumeration value="RECORD_NOT_FOUND"/>
 *     &lt;enumeration value="ROOM_UNAVAILABLE"/>
 *     &lt;enumeration value="INTERNAL_ERROR"/>
 *     &lt;enumeration value="INVALID_DATES"/>
 *     &lt;enumeration value="INVALID_GUEST_ID"/>
 *     &lt;enumeration value="EXCEPTION_THROWED"/>
 *     &lt;enumeration value="REGISTRY_CONNECTION_FAILURE"/>
 *     &lt;enumeration value="SERVER_CONNECTION_FAILURE"/>
 *     &lt;enumeration value="MGR_LOGIN_FAILURE"/>
 *     &lt;enumeration value="MSG_DECODE_ERR"/>
 *     &lt;enumeration value="TIME_OUT"/>
 *     &lt;enumeration value="INVALID_REQUEST"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "errorCode")
@XmlEnum
public enum ErrorCode {

    SUCCESS,
    HOTEL_NOT_FOUND,
    RECORD_NOT_FOUND,
    ROOM_UNAVAILABLE,
    INTERNAL_ERROR,
    INVALID_DATES,
    INVALID_GUEST_ID,
    EXCEPTION_THROWED,
    REGISTRY_CONNECTION_FAILURE,
    SERVER_CONNECTION_FAILURE,
    MGR_LOGIN_FAILURE,
    MSG_DECODE_ERR,
    TIME_OUT,
    INVALID_REQUEST;

    public String value() {
        return name();
    }

    public static ErrorCode fromValue(String v) {
        return valueOf(v);
    }

}
