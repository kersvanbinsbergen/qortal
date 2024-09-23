package org.qortal.network;

public class RNSCommon {

    /**
     * Destination application name
     */
    public static String MAINNET_APP_NAME = "qortal";      // production
    public static String TESTNET_APP_NAME = "qortaltest";  // test net

    /**
     * Configuration path relative to the Qortal launch directory
     */
    public static String defaultRNSConfigPath = ".reticulum";
    public static String defaultRNSConfigPathTestnet = ".reticulum_test";

    /**
     * Default config
     */
    public static String defaultRNSConfig = "reticulum_default_config.yml";
    public static String defaultRNSConfigTetnet = "reticulum_default_testnet_config.yml";

    ///**
    // * Qortal RNS Destinations
    // */
    //public enum RNSDestinations {
    //    BASE,
    //    QDN;
    //}
    
}
