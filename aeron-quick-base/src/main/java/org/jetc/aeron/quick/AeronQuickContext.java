package org.jetc.aeron.quick;

import io.aeron.Aeron;
import org.jetc.aeron.quick.messaging.serialization.JsonMapper;
import org.jetc.aeron.quick.messaging.serialization.ObjectStringMapper;

public class AeronQuickContext {
    private final String PROPS_SUFFIX = "aeron.quick.";
    private final ObjectStringMapper mapper = new JsonMapper();
    private Aeron aeron;

    public int getIntProperty(String component, String method, String prop){
       return Integer.parseInt(getProperty(component, method, prop));
   }

    public int getIntProperty(String component, String method, String prop, int defaultVal){
        String val = getProperty(component, method, prop);
        return val != null ? Integer.parseInt(val) : defaultVal;
    }

    public String getProperty(String component, String method, String prop){
        String fullPropStr = PROPS_SUFFIX + component + "." + method + "." + prop;
        String value = System.getProperty(fullPropStr);

        if(value == null || value.isBlank()) {
            value = System.getProperty(PROPS_SUFFIX + component + "." + prop);
        }

        if(value == null || value.isBlank()) {
            value = System.getProperty(PROPS_SUFFIX + prop);
        }

        return value;
    }

    public Aeron getAeron(){
        return aeron;
    }

    public void setAeron(Aeron aeron) {
        this.aeron = aeron;
    }

    public ObjectStringMapper getObjectMapper(){
        return mapper;
    }
}
