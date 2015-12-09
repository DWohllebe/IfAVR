package com.vr.daso.ifavr;

/**
 * Container class for material information.
 */
public class Material {
    public String name;
    public float Ns;
    public float[/*3*/] Ka;
    public float[/*3*/] Kd;
    public float[/*3*/] Ks;
    public float[/*3*/] Ke;
    public float Ni;
    public float d;
    public String illum;
    public String map_Kd;
    public String map_Bump;
    public String map_Ks;
}
