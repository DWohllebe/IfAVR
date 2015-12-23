package com.vr.daso.ifavr;

/**
 * Created by ASUS-X56T on 23.12.2015.
 */
//Variablenname Datentyp Adresse Kommentar Min Max Access-Mode Fractiondigits
//        Auto_Durchfl_FL BOOL 001008 Automatikbetrieb (Durchflussregelung) Ein/Aus über FactoryLink rw
//        Auto_Fuellst_FL BOOL 001009 Automatikbetrieb (Füllstandsregelung) Ein/Aus über FactoryLink rw
//        Auto_Temp_FL BOOL 001010 Automatikbetrieb (Temperaturregelung) Ein/Aus über FactoryLink rw
//        BehaelterDurch_FL INT 401692 Behälter für Durchflussregelung (Vorgabe über FactoryLink) 1 2 rw
//        BehaelterFuell_FL INT 401708 Behälter für Füllstandsregelung (Vorgabe über FactoryLink) 1 3 rw
//        Behaelter_A_FL INT 401112 Behälter, aus dem abgepumpt werden soll (Vorgabe über FactoryLink) 1 3 rw
//        Behaelter_B_FL INT 401128 Behälter, in den gepumpt werden soll (Vorgabe über FactoryLink) 1 3 rw
//        Durchfluss1_Ist REAL 400276 Istwert Durchfluss 1 r 1
//        Durchfluss2_Ist REAL 400308 Istwert Durchfluss 2 r 1
//        Durchfluss_Soll_FL REAL 401464 Sollwert Durchfluss (Vorgabe über FactoryLink) 0,5 1,8 rw 1
//        Fuellstand1_Ist REAL 400244 Istwert Füllstand in B1 r 0
//        Fuellstand2_Ist REAL 400180 Istwert Füllstand in B2 r 0
//        Fuellstand3_Ist REAL 400212 Istwert Füllstand in B3 r 0
//        Fuellstand_Soll_FL INT 401500 Sollwert Füllstand (Vorgabe über FactoryLink) 35 279 rw
//        LH1 BOOL 000152 kapazitiver Melder B1 oben (LH1=1 --> B1 voll) r
//        LH2 BOOL 000154 kapazitiver Melder B2 oben (LH2=1 --> B2 voll) r
//        LH3 BOOL 000156 kapazitiver Melder B3 oben (LH3=1 --> B3 voll) r
//        LL1 BOOL 000151 kapazitiver Melder B1 unten (LL1=0 --> B1 leer) r
//        LL2 BOOL 000153 kapazitiver Melder B2 unten (LL2=0 --> B2 leer) r
//        LL3 BOOL 000155 kapazitiver Melder B3 unten (LL3=0 --> B3 leer) r
//        M BOOL 000017 Rührer einschalten r
//        P1 BOOL 000015 Pumpe P1 r
//        P2 BOOL 000016 Pumpe P2 r
//        P3 INT 400100 Pumpendrehzahl 100% (Pumpe 3) r
//        P3_FL INT 401032 Handstellwert P3 0..100% (Vorgabe über FactoryLink) 0 100 rw
//        Schritt1 BOOL 000100 Merker - Umpumpen von B1 nach B2 (Demo-Betrieb) r
//        Schritt2 BOOL 000101 Merker - Umpumpen von B3 nach B2 (Demo-Betrieb) r
//        Schritt3 BOOL 000102 Merker - Füllstand auf 200mm einstellen (Demo-Betrieb) r
//        Schritt4 BOOL 000103 Merker - Umpumpen von B2 nach B3 (Demo-Betrieb) r
//        Schritt5 BOOL 000104 Merker - Durchfluß auf 1.5 l/min einstellen (Demo-Betrieb) r
//        Schritt6 BOOL 000105 Merker - Rührer einschalten (Demo-Betrieb) r
//        Start_Ablassen_FL BOOL 001002 Wasser aus Anlage ablassen Ein/Aus (Vorgabe über FactoryLink) rw
//        Start_Demo_FL BOOL 001001 Demoprogramm Ein/Aus (Vorgabe über FactoryLink) rw
//        Start_Dosieren_FL BOOL 001004 Dosieren Ein/Aus (Vorgabe über FactoryLink) rw
//        Start_Durchfluss_FL BOOL 001007 Durchflussregelung Ein/Aus (Vorgabe über FactoryLink) rw
//        Start_Fuellstand_FL BOOL 001006 Füllstandsregelung Ein/Aus (Vorgabe in FactoryLink) rw
public class Pumpstation {
    boolean Auto_Durchfl_FL = false;
    boolean Auto_Fuellst_FL = false;
    boolean Auto_Temp_FL = false;
    int BehaelterDurch_FL = -1;
    int BehaelterFuell_FL = -1;
    int Behaelter_A_FL = -1;
    int Behaelter_B_FL = -1;
    float Durchfluss1_Ist = -1.0f;
    float Durchfluss2_Ist = -1.0f;
    float Durchfluss_Soll_FL = -1.0f;
    float Fuellstand1_Ist = -1.0f;
    float Fuellstand2_Ist = -1.0f;
    float Fuellstand3_Ist = -1.0f;
    int Fuellstand_Soll_FL = -1;
    boolean LH1 = false;
    boolean LH2 = false;
    boolean LH3 = false;
    boolean LL1 = false;
    boolean LL2 = false;
    boolean LL3 = false;
    boolean M = false;
    boolean P1 = false;
    boolean P2 = false;
    int P3 = -1;
    int P3_FL = -1;
    boolean Schritt1 = false;
    boolean  Schritt2 = false;
    boolean Schritt3 = false;
    boolean Schritt4 = false;
    boolean  Schritt5 = false;
    boolean Schritt6 = false;
    boolean  Start_Ablassen_FL = false;
    boolean Start_Demo_FL = false;
    boolean Start_Dosieren_FL = false;
    boolean Start_Durchfluss_FL = false;
    boolean Start_Fuellstand_FL = false;
}
