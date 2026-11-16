package com.dinsaren.springbootjwtapi.payload.pos;

import lombok.Data;
import java.util.List;

@Data
public class RoleUpdateRequest {
    private String role;
    private List<String> pages;
}
