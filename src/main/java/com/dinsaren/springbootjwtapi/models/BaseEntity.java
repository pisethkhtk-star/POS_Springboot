package com.dinsaren.springbootjwtapi.models;

import com.dinsaren.springbootjwtapi.constants.Constants;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
@MappedSuperclass
public class BaseEntity implements Serializable {
    private static final long serialVersionUID = -8892838641805537110L;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();

    @Column(name = "created_by")
    private String createdBy = Constants.SYSTEM;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    // Backward compatibility aliases
    public Date getCreateAt() {
        return createdAt;
    }

    public void setCreateAt(Date createAt) {
        this.createdAt = createAt;
    }

    public String getCreateBy() {
        return createdBy;
    }

    public void setCreateBy(String createBy) {
        this.createdBy = createBy;
    }

    public Date getUpdateAt() {
        return updatedAt;
    }

    public void setUpdateAt(Date updateAt) {
        this.updatedAt = updateAt;
    }

    public String getUpdateBy() {
        return updatedBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updatedBy = updateBy;
    }
}
