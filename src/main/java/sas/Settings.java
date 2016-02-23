package sas;

import architectures.Scenario1;


public class Settings {
	public static Class<? extends ArchitectureTemplate> architecture = Scenario1.class;
	
	public static final Integer MAX_EXPLOITABILITY_RATE = Integer.MAX_VALUE;
	
	public static final Integer MAX_PARALLEL_ECU_EXPLOITS = 1;
	public static final Integer MAX_PARALLEL_INTERFACE_FR_EXPLOITS = 1;
	public static final Integer MAX_PARALLEL_INTERFACE_FR_BG_EXPLOITS = 1;
	public static final Integer MAX_PARALLEL_INTERFACE_CAN_EXPLOITS = 1;
	public static final Integer MAX_PARALLEL_INTERFACE_3G_EXPLOITS = 1;
	public static final Integer MAX_PARALLEL_MSG_EXPLOITS = 1;
	
	public static final String UTILIZATION = "utilization";
	
	public static final Boolean PDF_OUTPUT = false;
	public static final Boolean PRISM_EXPORT = false;
}
