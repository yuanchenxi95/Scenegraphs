package sgraph.Nodes;

import com.jogamp.opengl.util.texture.Texture;
import org.joml.Matrix4f;
import sgraph.IScenegraph;
import sgraph.IScenegraphRenderer;
import util.Light;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * This interface represents all the operations offered by any type of node in our scenegraph.
 * Not all types of nodes are able to offer all types of operations.
 * This is implemented by the {@link AbstractNode} throwing an exception for all such methods, and
 * appropriate nodes overriding these methods
 *
 * @author Amit Shesh
 */
public interface INode {
  /**
   * In the scene graph rooted at this node, get the node whose name is as given
   *
   * @param name name of node to be searched
   * @return the node reference if it exists, null otherwise
   */
  INode getNode(String name);

  /**
   * Draw the scene graph rooted at this node, using the modelview stack and context
   *
   * @param context   the generic renderer context {@link sgraph.IScenegraphRenderer}
   * @param modelView the stack of modelview matrices
   */
  void draw(IScenegraphRenderer context, Stack<Matrix4f> modelView);

  /**
   * Return a deep copy of the scene graph subtree rooted at this node
   *
   * @return a reference to the root of the copied subtree
   */
  public INode clone();

  /**
   * Set the parent of this node. Each node except the root has a parent
   *
   * @param parent the node that is to be the parent of this node
   */
  void setParent(INode parent);

  /**
   * Traverse the scene graph rooted at this node, and store references to the scenegraph object
   *
   * @param graph a reference to the scenegraph object of which this tree is a part
   */

  void setScenegraph(IScenegraph graph);

  /**
   * Set the name of this node. The name is not guaranteed to be unique in the tree, but it should be.
   *
   * @param name the name of this node
   */
  void setName(String name);


  /**
   * Get the name of this node
   *
   * @return the name of this node
   */
  String getName();

  /**
   * Add a child to this node. Not all types of nodes have the capability of having children.
   * If the node cannot have a child, this method throws an {@link }IllegalArgumentException}
   *
   * @param node the node that must be added as a child to this node
   * @throws {@link }IllegalArgumentException} if this node is unable to have children (i.e. leaves)
   */
  void addChild(INode node) throws IllegalArgumentException;

  /**
   * Set the transformation associated with this node. Not all types of nodes can have transformations.
   * If the node cannot store a transformation, this method throws an {@link }IllegalArgumentException}
   *
   * @param m the tranformation matrix associated with this transformation
   * @throws {@link }IllegalArgumentException} if this node is unable to store a transformation (all nodes except TransformNode)
   */
  void setTransform(Matrix4f m) throws IllegalArgumentException;

  /**
   * Set the transformation associated with this node. Not all types of nodes can have transformations.
   * If the node cannot store a transformation, this method throws an {@link }IllegalArgumentException}
   *
   * @param m the tranformation matrix associated with this transformation
   * @throws IllegalArgumentException if this node is unable to store a transformation (all nodes except TransformNode)
   */
  void setAnimationTransform(Matrix4f m) throws IllegalArgumentException;

  /**
   * Set the material associated with this node. Not all types of nodes can have materials associated with them.
   * If the node cannot have a material, this method throws an {@link }IllegalArgumentException}
   *
   * @param m the material object to be associated with this node
   * @throws {@link }IllegalArgumentException} if this node is unable to store a material (all nodes except leaves)
   */
  void setMaterial(util.Material m) throws IllegalArgumentException;

  /**
   * Set the texture associated with this node. Not all types of nodes can have textures associated with them.
   * If the node cannot have a texture, this method throws an {@link }IllegalArgumentException}
   *
   * @param m the material object to be associated with this node
   * @throws {@link }IllegalArgumentException} if this node is unable to store a material (all nodes except leaves)
   */
  void setTexture(Texture m) throws IllegalArgumentException;


  /**
   * Get the transform of this matrix related to the world
   *
   * @param accumulator the accumulator
   * @param worldToView the camera matrix
   * @return the transform of this matrix
   */
  Matrix4f getObjectToViewTransform(Matrix4f accumulator, Matrix4f worldToView);

  /**
   * Add the light to the list of lights
   *
   * @param light the light needed to be add
   * @throws {@link }IllegalArgumentException} if this node is unable to store a Light
   */
  void addLight(util.Light light) throws IllegalArgumentException;


  /**
   * Get the list of lights associated with this node
   *
   * @return the list lights asscociated with this node
   */
  ArrayList<Light> getLights();

  /**
   * Get the list of all lights within this node and its children.
   *
   * @param accLights   the accumulator
   * @param unvisited   the unvisited list
   * @param worldToView
   * @return all lights in this node and its children
   */
  List<util.Light> getAllLightsHelp(List<Light> accLights, List<INode> unvisited, Matrix4f worldToView);

  /**
   * Get the list of all lights within this node and its children.
   *
   * @param worldToView
   * @return all lights in this node and its children
   */
  List<util.Light> getAllLights(Matrix4f worldToView);

  void transformLightsPassedIn(Matrix4f toView, List<Light> listOfLightsTransformed);

}

