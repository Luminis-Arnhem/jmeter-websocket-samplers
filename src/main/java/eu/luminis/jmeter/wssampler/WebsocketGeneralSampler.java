package eu.luminis.jmeter.wssampler;

import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.NullProperty;

public abstract class WebsocketGeneralSampler extends WebsocketSampler {

    private static final String PROPERTY_PAYLOAD = "payloadType";
    private static final String PROPERTY_PAYLOAD_LEGACY_IS_BINARY = "binaryPayload";

    private static final DataPayloadType PROPERTY_PAYLOAD_DEFAULT = DataPayloadType.Text;

    public DataPayloadType getType() {
        final JMeterProperty value = getProperty(PROPERTY_PAYLOAD);
        if (value == null || value instanceof NullProperty) {
            // no stored value, fallback to legacy property
            final JMeterProperty legacyValue = getProperty(PROPERTY_PAYLOAD_LEGACY_IS_BINARY);
            if (legacyValue == null || legacyValue instanceof NullProperty) {
                // no legacy value, fallback to default value
                return PROPERTY_PAYLOAD_DEFAULT;
            } else {
                // got legacy property value, convert  to a new one
                final boolean binaryPayload = legacyValue.getBooleanValue();
                return binaryPayload ? DataPayloadType.Binary : DataPayloadType.Text;
            }
        } else {
            try {
                return DataPayloadType.valueOf(value.getStringValue());
            } catch (IllegalArgumentException e) {
                getLogger().warn("Wrong value found for payloadType: '" + value + "'");
                return PROPERTY_PAYLOAD_DEFAULT;
            }
        }
    }

    public void setType(final DataPayloadType type) {
        setProperty(PROPERTY_PAYLOAD, type.name());
    }
}
