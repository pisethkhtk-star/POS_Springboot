package com.dinsaren.springbootjwtapi.payload.pos;

import com.dinsaren.springbootjwtapi.models.pos.PosOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderPageResponse {
    private List<PosOrder> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private double totalRevenue;
}
