package util;

import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Created by ashesh on 2/22/2016.
 */
public class Light {
    private Vector3f ambient, diffuse, specular;
    private Vector4f position, spotDirection;
    private float spotCutoff;

    public Light() {
        ambient = new Vector3f(0, 0, 0);
        diffuse = new Vector3f(0, 0, 0);
        specular = new Vector3f(0, 0, 0);

        position = new Vector4f(0, 0, 0, 1);
        spotDirection = new Vector4f(0, 0, 0, 0);
        spotCutoff = 0.0f;
    }

    public void setAmbient(float r, float g, float b) {
        ambient = new Vector3f(r, g, b);
    }

    public void setDiffuse(float r, float g, float b) {
        diffuse = new Vector3f(r, g, b);
    }

    public void setSpecular(float r, float g, float b) {
        specular = new Vector3f(r, g, b);
    }

    public void setPosition(float x, float y, float z) {
        position = new Vector4f(x, y, z, 1.0f);
    }

    public void setDirection(float x, float y, float z) {
        position = new Vector4f(x, y, z, 0.0f);
    }

    public void setSpotDirection(float x, float y, float z) {
        spotDirection = new Vector4f(x, y, z, 0.0f);
    }

    public void setSpotAngle(float angle) {
        spotCutoff = (float) Math.cos(Math.toRadians(angle));
    }

    public void setSpotCutoff(float cutoff) {
        spotCutoff = cutoff;
    }

    ////////////////////////////////////////////////////////////////////////
    public void setAmbient(Vector3f amb) {
        ambient = new Vector3f(amb);
    }

    public void setDiffuse(Vector3f diff) {
        diffuse = new Vector3f(diff);
    }

    public void setSpecular(Vector3f spec) {
        specular = new Vector3f(spec);
    }

    public void setPosition(Vector4f pos) { position = new Vector4f(pos); }

    public void setDirection(Vector4f dir) { position = new Vector4f(dir); }

    public void setSpotDirection(Vector4f dir) { spotDirection = new Vector4f(dir); }

    public Vector3f getAmbient() {
        return new Vector3f(ambient);
    }

    public Vector3f getDiffuse() {
        return new Vector3f(diffuse);
    }

    public Vector3f getSpecular() {
        return new Vector3f(specular);
    }

    public Vector4f getPosition() {
        return new Vector4f(position);
    }

    public Vector4f getSpotDirection() {
        return new Vector4f(spotDirection);
    }

    public float getSpotCutoff() {
        return spotCutoff;
    }

    @Override
    public Light clone() {
        Light l = new Light();
        l.setAmbient(this.getAmbient());
        l.setDiffuse(this.getDiffuse());
        l.setSpecular(this.getSpecular());
        l.setPosition(this.getPosition());
        l.setSpotDirection(this.getSpotDirection());
        l.setSpotCutoff(this.getSpotCutoff());
        return l;
    }

}

