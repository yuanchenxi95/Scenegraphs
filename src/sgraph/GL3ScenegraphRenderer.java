package sgraph;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import org.joml.Matrix4f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
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
     * A variable tracking whether shader locations have been set. This must be done before
     * drawing!
     */
    private boolean shaderLocationsSet;

    public GL3ScenegraphRenderer()
    {
        meshRenderers = new TreeMap<String,GL3MeshRenderer>();
        shaderLocationsVault = new TreeMap<String,Integer>();
        shaderLocationsSet = false;
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

    /**
     * Begin rendering of the scene graph from the root
     * @param root
     * @param modelView
     */
    @Override
    public void draw(INode root, Stack<Matrix4f> modelView)
    {
        root.draw(this,modelView);
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
     */
    @Override
    public void drawMesh(String name, util.Material material,final Matrix4f transformation)
    {
        if (meshRenderers.containsKey(name))
        {
            GL3 gl = glContext.getGL().getGL3();
            //get the color

            FloatBuffer fb = FloatBuffer.allocate(4);

            int loc = shaderLocationsVault.get("vColor");
            //set the color for all vertices to be drawn for this object
            if (loc<0)
                throw new IllegalArgumentException("No shader variable for \" vColor \"");

            gl.glUniform3fv(loc,1,material.getAmbient().get(fb));

            loc = shaderLocationsVault.get("modelview");
            if (loc<0)
                throw new IllegalArgumentException("No shader variable for \" modelview \"");

            fb = FloatBuffer.allocate(16);
            gl.glUniformMatrix4fv(loc,1,false,transformation.get(fb));

            meshRenderers.get(name).draw();
        }
    }

    /**
     * Queries the shader program for all variables and locations, and adds them to itself
     * @param shaderProgram
     */
    @Override
    public void initShaderProgram(util.ShaderProgram shaderProgram)
    {
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
