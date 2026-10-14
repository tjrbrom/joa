package com.x.hhqs.joa.in.vo;

public class PortMacAddress implements Comparable<PortMacAddress> {
	@NbiResponseDefinition(fieldName = "VLAN")
	private String vlan;

	@NbiResponseDefinition(fieldName = "MAC")
	private String mac;

	public PortMacAddress() {
		super();
	}

	public String getVlan() {
		return vlan;
	}

	public void setVlan(String vlan) {
		this.vlan = vlan;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	@Override
	public int compareTo(PortMacAddress o) {
		return this.getMac().hashCode() - o.getMac().hashCode();
	}

	@Override
	public String toString() {
		return "PortMacAddress [vlan=" + vlan + ", mac=" + mac + "]";
	}

}
