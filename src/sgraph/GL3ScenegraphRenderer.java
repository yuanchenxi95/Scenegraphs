package sgraph;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import org.joml.Matrix4f;
import sgraph.Nodes.INode;
import util.Light;
import util.Material;
import util.TextureImage;

import java.nio.FloatBuffer;
import java.util.*;

/**
 * This is a scene graph renderer implementation that works specifically with the JOGL library
 * It mandates OpenGL 3 and above.
 * @author Amit Shesh
 */
public class GL3ScenegraphRenderer implements IScenegraphRenderer {
    /**
     * The JOGL specific rendering context
     */
    private GLAutoDrawable glContext;
    /**
     * A table of shader locations and variable names
     */
    protected Map<String,Integer> shaderLocationsVault;
    /**
     * A table of renderers for individual meshes
     */
    private Map<String,GL3MeshRenderer> meshRenderers;

    /**
     * A table of renderers for individual texture
     */
    private Map<String,TextureImage> textureRenderers;



    ////////////////////////////////////////////////////////

    util.ShaderProgram program;



    class LightLocation
    {
        int ambient,diffuse,specular,position, spotDirection, spotCutoff;;
        public LightLocation()
        {
            ambient = diffuse = specular = position = spotDirection = spotCutoff = -1;


        }
    }


    private List<LightLocation> lightLocations;
    int angleOfRotation = 0;



    ////////////////////////////////////////////////////////

    /**
     * A variable tracking whether shader locations have been set. This must be done before
     * drawing!
     */
    private boolean shaderLocationsSet;

    public GL3ScenegraphRenderer()
    {
        meshRenderers = new TreeMap<String,GL3MeshRenderer>();
        textureRenderers = new TreeMap<String, TextureImage>();
        shaderLocationsVault = new TreeMap<String,Integer>();
        shaderLocationsSet = false;

        lightLocations = new ArrayList<LightLocation>();


    }

    /**
     * Specifically checks if the passed rendering context is the correct JOGL-specific
     * rendering context {@link com.jogamp.opengl.GLAutoDrawable}
     * @param obj the rendering context (should be {@link com.jogamp.opengl.GLAutoDrawable})
     * @throws IllegalArgumentException if given rendering context is not {@link com.jogamp.opengl.GLAutoDrawable}
     */
    @Override
    public void setContext(Object obj) throws IllegalArgumentException
    {
        if (obj instanceof GLAutoDrawable)
        {
            glContext = (GLAutoDrawable)obj;
        }
        else
            throw new IllegalArgumentException("Context not of type GLAutoDrawable");
    }

    /**
     * Add a mesh to be drawn later.
     * The rendering context should be set before calling this function, as this function needs it
     * This function creates a new {@link sgraph.GL3MeshRenderer} object for this mesh
     * @param name the name by which this mesh is referred to by the scene graph
     * @param mesh the {@link util.PolygonMesh} object that represents this mesh
     * @throws Exception
     */
    @Override
    public void addMesh(String name,util.PolygonMesh mesh) throws Exception
    {
        if (!shaderLocationsSet)
            throw new Exception("Attempting to add mesh before setting shader variables. Call initShaderProgram first");
        if (glContext==null)
            throw new Exception("Attempting to add mesh before setting GL context. Call setContext and pass it a GLAutoDrawable first.");
        GL3MeshRenderer mr = new GL3MeshRenderer();
        mr.setGL(glContext);
        mr.prepare(mesh,shaderLocationsVault);
        meshRenderers.put(name,mr);
    }

    @Override
    public void addTexture(String name, TextureImage tex) throws Exception {
        if (!shaderLocationsSet)
            throw new Exception("Attempting to add mesh before setting shader variables. Call initShaderProgram first");
        if (glContext==null)
            throw new Exception("Attempting to add mesh before setting GL context. Call setContext and pass it a GLAutoDrawable first.");
        textureRenderers.put(name,tex);

        TextureImage textureImage = textureRenderers.get(name);

        GL3 gl = glContext.getGL().getGL3();

        Texture texture = textureImage.getTexture();
        if (texture != null) {

            texture.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
            texture.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
            texture.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
            texture.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
            int a = gl.glGetError();

        }

    }


    @Override
    public void addLights(List<Light> lights) throws Exception {
        GL3 gl = glContext.getGL().getGL3();



        for(int i = 0; i < lights.size(); i++) {
            LightLocation ll = new LightLocation();
            String name;

            System.out.println(lights.get(i).getSpotCutoff());

            name = "light[" + i + "]";
            ll.ambient = program.getUniformLocation(gl, name + ".ambient");
            ll.diffuse = program.getUniformLocation(gl, name + ".diffuse");
            ll.specular = program.getUniformLocation(gl, name + ".specular");
            ll.position = program.getUniformLocation(gl, name + ".position");
            ll.spotDirection = program.getUniformLocation(gl, name + ".spotDirection");
            ll.spotCutoff = program.getUniformLocation(gl, name + ".spotCutoff");

            lightLocations.add(ll);
        }
    }

    /**
     * Begin rendering of the scene graph from the root
     * @param root
     * @param modelView
     */
    @Override
    public void draw(INode root, Stack<Matrix4f> modelView)
    {
        GL3 gl = glContext.getGL().getGL3();

//        gl.glEnable(GL_TEXTURE_2D);
      //  gl.glActiveTexture(GL.GL_TEXTURE0);
       // gl.glUniform1i(shaderLocationsVault.get("image"),0);



        root.draw(this,modelView);
        drawLight(root.getAllLights(modelView.peek()));


    }

    @Override
    public void dispose()
    {
        for (GL3MeshRenderer s:meshRenderers.values())
            s.dispose();
    }

    /**
     * Draws a specific mesh.
     * If the mesh has been added to this renderer, it delegates to its correspond mesh renderer
     * This function first passes the material to the shader. Currently it uses the shader variable
     * "vColor" and passes it the ambient part of the material. When lighting is enabled, this method must
     * be overriden to set the ambient, diffuse, specular, shininess etc. values to the shader
     * @param name
     * @param material
     * @param transformation
     * @param texturename
     */
    @Override
    public void drawMesh(String name, Material material, final Matrix4f transformation, String texturename) {
        if (meshRenderers.containsKey(name)) {
            GL3 gl = glContext.getGL().getGL3();

            FloatBuffer fb4 = FloatBuffer.allocate(4);
            FloatBuffer fb16 = FloatBuffer.allocate(16);

            int loc = shaderLocationsVault.get("material.ambient");

            // AMBIENT
            if (loc < 0)
                throw new IllegalArgumentException("No shader variable for \" ambient \"\"");
            gl.glUniform3fv(loc, 1, material.getAmbient().get(fb4));


            // DIFFUSE
            loc = shaderLocationsVault.get("material.diffuse");
            if (loc < 0)
                throw new IllegalArgumentException("No shader variable for \" diffuse \"\"");
            gl.glUniform3fv(loc, 1, material.getDiffuse().get(fb4));

            // SPECULAR
            loc = shaderLocationsVault.get("material.specular");
            if (loc < 0)
                throw new IllegalArgumentException("No shader variable for \" diffuse \"\"");
            gl.glUniform3fv(loc, 1, material.getSpecular().get(fb4));

            // SHININESS
            loc = shaderLocationsVault.get("material.shininess");
            if (loc < 0)
                throw new IllegalArgumentException("No shader variable for \" shininess \"\"");
            gl.glUniform1f(loc, material.getShininess());

//            // ABSORPTION
//            loc = shaderLocationsVault.get("material.absorption");
//            if (loc < 0)
//                throw new IllegalArgumentException("No shader variable for \" absorption \"\"");
//            gl.glUniform1f(loc, material.getAbsorption());
//
//            // REFLECTION
//            loc = shaderLocationsVault.get("material.reflection");
//            if (loc < 0)
//                throw new IllegalArgumentException("No shader variable for \" reflection \"\"");
//            gl.glUniform1f(loc, material.getReflection());
//
//            // TRANSPARENCY
//            loc = shaderLocationsVault.get("material.transparency");
//            if (loc < 0)
//                throw new IllegalArgumentException("No shader variable for \" transparency \"\"");
//            gl.glUniform1f(loc, material.getTransparency());
//
//            // REFRACTIVE
//            loc = shaderLocationsVault.get("material.refractive");
//            if (loc < 0)
//                throw new IllegalArgumentException("No shader variable for \" refractive \"\"");
//            gl.glUniform1f(loc, material.getRefractiveIndex());




            // MODEL VIEW
            loc = shaderLocationsVault.get("modelview");
            if (loc < 0)
                throw new IllegalArgumentException("No shader variable for \" modelview \"");
            gl.glUniformMatrix4fv(loc, 1, false, transformation.get(fb16));

            loc = shaderLocationsVault.get("normalmatrix");
            gl.glUniformMatrix4fv(loc, 1, false, transformation.invert().transpose().get(fb16));



            Matrix4f textureTransform = new Matrix4f();


            if(textureRenderers.containsKey(texturename)) {
                // if textures does not have texture
                // initialize this texture


                TextureImage textureImage = textureRenderers.get(texturename);

                Texture texture = textureImage.getTexture();

                if (texture.getMustFlipVertically()) //for flipping the image vertically
                {
                    textureTransform = new Matrix4f().translate(0,1,0).scale(1,-1,1);
                }

                texture.bind(gl);



            } else {
                //TO-DO
                // pass in white texture


                if(textureRenderers.containsKey("white-texture")) {

                } else {
                    throw new IllegalArgumentException("\"white-texture\" missed.\n" +
                            "Must pass in a default texture with a name of \"white-texture\"");
                }

                TextureImage textureImage = textureRenderers.get("white-texture");

                Texture texture = textureImage.getTexture();


                if (!texture.getMustFlipVertically()) //for flipping the image vertically
                {
                    textureTransform = new Matrix4f().translate(0,1,0).scale(1,-1,1);
                }

                texture.bind(gl);

            }


            loc = shaderLocationsVault.get("texturematrix");
            gl.glUniformMatrix4fv(loc, 1, false, textureTransform.get(fb16));



            meshRenderers.get(name).draw();

        }

    }


    @Override
    public void drawLight(List<Light> lights) {
        GL3 gl = glContext.getGL().getGL3();

        FloatBuffer fb4 = FloatBuffer.allocate(4);


        gl.glUniform1i(shaderLocationsVault.get("numLights"), lights.size());
        for (int i = 0; i < lightLocations.size(); i++) {


            gl.glUniform3fv(lightLocations.get(i).ambient,1,lights.get(i).getAmbient().get(fb4));
            gl.glUniform3fv(lightLocations.get(i).diffuse,1,lights.get(i).getDiffuse().get(fb4));
            gl.glUniform3fv(lightLocations.get(i).specular,1,lights.get(i).getSpecular().get(fb4));
            gl.glUniform4fv(lightLocations.get(i).position,1,lights.get(i).getPosition().get(fb4));
            gl.glUniform4fv(lightLocations.get(i).spotDirection,1,lights.get(i).getSpotDirection().get(fb4));
            gl.glUniform1f(lightLocations.get(i).spotCutoff, lights.get(i).getSpotCutoff());


//            System.out.println("The position of the light " + i + ": " + lights.get(i).getPosition());

        }


    }



    /**
     * Queries the shader program for all variables and locations, and adds them to itself
     * @param shaderProgram
     */
    @Override
    public void initShaderProgram(util.ShaderProgram shaderProgram)
    {
        program = shaderProgram;

        Objects.requireNonNull(glContext);
        GL3 gl = glContext.getGL().getGL3();

        shaderLocationsVault = shaderProgram.getAllShaderVariables(gl);

        int a = gl.glGetError();
        shaderLocationsSet = true;


    }


    @Override
    public int getShaderLocation(String name)
    {
        if (shaderLocationsVault.containsKey(name))
            return shaderLocationsVault.get(name);
        else
            return 0;
    }




}
