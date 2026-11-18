package com.dinsaren.springbootjwtapi.services.pos;

import java.util.Map;

public interface PosKhqrService {
    Map<String, Object> generateKhqr(Map<String, Object> body);
}
