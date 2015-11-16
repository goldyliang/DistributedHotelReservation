
package client.webservice;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for roomType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="roomType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="SINGLE"/>
 *     &lt;enumeration value="DOUBLE"/>
 *     &lt;enumeration value="FAMILY"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "roomType")
@XmlEnum
public enum RoomType {

    SINGLE,
    DOUBLE,
    FAMILY;

    public String value() {
        return name();
    }

    public static RoomType fromValue(String v) {
        return valueOf(v);
    }

}
