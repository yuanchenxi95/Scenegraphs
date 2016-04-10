package sgraph;

import org.joml.Matrix4f;
import sgraph.Nodes.INode;
import util.Light;
import util.Material;

import java.util.List;
import java.util.Stack;

/**
 * This interface provides a general interface for the scene graph to use.
 * Each scene graph is paired with a renderer. Specific implementations of this renderer
 * will encapsulate rendering-specific code (e.g. OpenGL), but this interface itself is
 * independent of specific rendering libraries. This helps in keeping the scene graph
 * independent of specific rendering technologies.
 * @author Amit Shesh
 */
public interface IScenegraphRenderer
{
    /**
     * Set a rendering context. Renderers often need a rendering context (e.g. the windowing
     * context, etc.). The parameter is kept very general to support any library. Specific
     * implementations must check if the type matches what they expect.
     * @param obj the rendering context
     * @throws IllegalArgumentException thrown if the type of the rendering context is not as expected.
     */
    void setContext(Object obj) throws IllegalArgumentException;

    /**
     * Initialize the renderer with the shader program. This will also read all relevant shader
     * variables that it must set
     * @param shaderProgram
     */
    void initShaderProgram(util.ShaderProgram shaderProgram);

    /**
     * Get the location of a particular shader variable. Renderers for individual meshes will need
     * this to provide mesh-specific properties like material to the shaders.
     * The intention is that the renderer stores only what is required to render it, not
     * necessarily the entire mesh itself.
     * @param name
     * @return an integer handle for the appropriate variable if it exists, -1 otherwise
     */
    int getShaderLocation(String name);

    /**
     * Add a mesh to be rendered in the future.
     * @param name the name by which this mesh is referred to by the scene graph
     * @param mesh the {@link util.PolygonMesh} object that represents this mesh
     * @throws Exception general mechanism to let the scene graph know of any problems
     */
    void addMesh(String name,util.PolygonMesh mesh) throws Exception;

    /**
     * Add a texture to be rendered in the future
     * @param name the name by which this texture is refered to by the scene graph
     * @param tex the {@link util.TextureImage} object that represents this mesh
     * @throws Exception general mechanism to let the scene graph know of any problems
     */
    void addTexture(String name, util.TextureImage tex) throws Exception;

    /**
     * Add lights to be rendered in the future
     * @param lights the list of lights needed to be added in
     * @throws Exception general mechanism to let the scene graph know of any problems
     */
    void addLights(List<Light> lights) throws Exception;

    /**
     * Draw the scene graph rooted at supplied node using the supplied modelview stack.
     * This is usually called by the scene graph
     * @param root
     * @param modelView
     */
    void draw(INode root, Stack<Matrix4f> modelView);

    /**
     * Draw a specific mesh. This is called from a leaf node of the associated scene graph
     * @param name
     * @param material
     * @param transformation
     * @param texture
     */
    void drawMesh(String name, Material material, final Matrix4f transformation, String texture);

    /**
     * Draw a specific light.
     * @param lol the list of lights
     */
    void drawLight(List<Light> lol);

    void dispose();
}
