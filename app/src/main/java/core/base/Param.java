package core.base;

import java.util.HashMap;

@SuppressWarnings("EmptyMethod")
public interface Param {
    byte[] makeRequestBody();

    HashMap<String, String> makeRequestHeaders();

    String makeBodyContentType();
}
