/*
 * Copyright © 2018 IBM Intellectual Property.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.ccsdk.apps.controllerblueprints.service.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.Proxy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * CbaContent.java Purpose: Provide Configuration Generator for CbaContent Entity
 *
 * @author Ruben Chang
 * @version 1.0
 */

@EntityListeners({AuditingEntityListener.class})
@Entity
@Table(name = "CBA_CONTENT")
@Proxy(lazy=false)
public class CbaContent implements Serializable {

    private static final long serialVersionUID = 1L;

    public CbaContent() {
        this.cbaUUID = UUID.randomUUID().toString();
    }

    @Id
    @Column(name = "cba_uuid", nullable = false)
    private String cbaUUID;

    @Lob
    @Column(name = "cba_file")
    private byte[] cbaFile;

    @Column(name = "cba_name")
    private String cbaName;

    @Column(name = "cba_version")
    private String cbaVersion;

    @Column(name = "cba_state")
    private int cbaState;

    @Column(name="cba_description")
    private String cbaDescription;

    @OneToMany(mappedBy = "configModelCBA", fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<ConfigModel> models = new ArrayList<>();

    public String getCbaUUID() {
        return cbaUUID;
    }

    public void setCbaUUID(String cbaUUID) {
        this.cbaUUID = cbaUUID;
    }

    public String getCbaName() {
        return cbaName;
    }

    public void setCbaName(String cbaName) {
        this.cbaName = cbaName;
    }

    public String getCbaVersion() {
        return cbaVersion;
    }

    public void setCbaVersion(String cbaVersion) {
        this.cbaVersion = cbaVersion;
    }

    public List<ConfigModel> getModels() {
        return models;
    }

    public void setModels(List<ConfigModel> models) { this.models = models; }

    public int getCbaState() { return cbaState; }

    public void setCbaState(int cbaState) { this.cbaState = cbaState; }

    public String getCbaDescription() { return cbaDescription; }

    public void setCbaDescription(String cbaDescription) { this.cbaDescription = cbaDescription; }

    public byte[] getCbaFile() { return cbaFile; }

    public void setCbaFile(byte[] cbaFile) { this.cbaFile = cbaFile; }

}
