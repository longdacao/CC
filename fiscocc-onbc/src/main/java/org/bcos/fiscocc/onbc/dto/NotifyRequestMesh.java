package org.bcos.fiscocc.onbc.dto;

import java.math.BigInteger;

public class NotifyRequestMesh {
	private String evidenceAddress;
    private String appid;
    private BigInteger seq;

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public BigInteger getSeq() {
        return seq;
    }

    public void setSeq(BigInteger seq) {
        this.seq = seq;
    }

    public String getEvidenceAddress() {
		return evidenceAddress;
	}

	public void setEvidenceAddress(String evidenceAddress) {
		this.evidenceAddress = evidenceAddress;
	}
}
