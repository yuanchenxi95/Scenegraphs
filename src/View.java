import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.GLBuffers;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import sgraph.INode;
import sgraph.Scenegraph;
import sgraph.TransformNode;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Stack;


/**
 * Created by ashesh on 9/18/2015.
 *
 * The View class is the "controller" of all our OpenGL stuff. It cleanly encapsulates all our OpenGL functionality from the rest of Java GUI, managed
 * by the JOGLFrame class.
 */
public class View
{
    private int WINDOW_WIDTH,WINDOW_HEIGHT;
    private Stack<Matrix4f> modelView;
    private Matrix4f projection,trackballTransform;
    private float trackballRadius;
    private Vector2f mousePos;
    private util.ObjectInstance meshObject;


    private util.ShaderProgram program;
    private int projectionLocation;
    private sgraph.IScenegraph scenegraph;
    private int angleOfRotation;

    private enum cameraState {
        STATIONARY,
        ONSPIDER
    }

    private cameraState cState= cameraState.STATIONARY;



    public View()
    {
        projection = new Matrix4f();
        modelView = new Stack<Matrix4f>();
        angleOfRotation = 0;
        trackballRadius = 300;
        trackballTransform = new Matrix4f();

    }

    public void initScenegraph(GLAutoDrawable gla,InputStream in) throws Exception
    {
        GL3 gl = gla.getGL().getGL3();

        if (scenegraph!=null)
            scenegraph.dispose();

        scenegraph = sgraph.SceneXMLReader.importScenegraph(in);

        sgraph.IScenegraphRenderer renderer = new sgraph.GL3ScenegraphRenderer();
        renderer.setContext(gla);
        renderer.initShaderProgram(program);
        scenegraph.setRenderer(renderer);
    }

    public void init(GLAutoDrawable gla) throws Exception
    {
        GL3 gl = gla.getGL().getGL3();




        //compile and make our shader program. Look at the ShaderProgram class for details on how this is done
        program = new util.ShaderProgram();

        program.createProgram(gl,"shaders/triangles.vert","shaders/triangles.frag");


        //get input variables that need to be given to the shader program
        projectionLocation = program.getUniformLocation(gl,"projection");
    }



    public void draw(GLAutoDrawable gla)
    {
        scenegraph.animate(angleOfRotation);
        angleOfRotation = (angleOfRotation+1)%360;
        GL3 gl = gla.getGL().getGL3();

        gl.glClearColor(0, 0, 0, 1);
        gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);
        gl.glEnable(GL.GL_DEPTH_TEST);


        program.enable(gl);

        while (!modelView.empty())
            modelView.pop();

        /*
         *In order to change the shape of this triangle, we can either move the vertex positions above, or "transform" them
         * We use a modelview matrix to store the transformations to be applied to our triangle.
         * Right now this matrix is identity, which means "no transformations"
         */
        modelView.push(new Matrix4f());

        switch (cState) {
            case STATIONARY:
                modelView.peek().lookAt(new Vector3f(0,500,400),new Vector3f(0,50,0),new Vector3f(0,1,0))
                        .mul(trackballTransform);
                break;
            case ONSPIDER:
//                INode rootNode = scenegraph.getRoot();

//                Matrix4f objectToWorld = modelView.peek().mul(tn.getAnimationTransform()).mul(tn.getTransform());
//                Matrix4f worldToObject = new Matrix4f(objectToWorld).invert();
                modelView.peek().lookAt(new Vector3f(0,0,0),new Vector3f(-1,0,0),new Vector3f(0,1,0));
                        //.mul(worldToObject).mul(trackballTransform);
                break;
            default:
                throw new IllegalArgumentException("camera state is wrong");
        }

    /*
     *Supply the shader with all the matrices it expects.
    */
        FloatBuffer fb = Buffers.newDirectFloatBuffer(16);
        gl.glUniformMatrix4fv(projectionLocation,1,false,projection.get(fb));
        //return;


//        gl.glPolygonMode(GL.GL_FRONT_AND_BACK,GL3.GL_LINE); //OUTLINES
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK,GL3.GL_FILL); //FILLED

        scenegraph.draw(modelView);
    /*
     *OpenGL batch-processes all its OpenGL commands.
          *  *The next command asks OpenGL to "empty" its batch of issued commands, i.e. draw
     *
     *This a non-blocking function. That is, it will signal OpenGL to draw, but won't wait for it to
     *finish drawing.
     *
     *If you would like OpenGL to start drawing and wait until it is done, call glFinish() instead.
     */
        gl.glFlush();

        program.disable(gl);



    }

    public void keyTyped(char c) {
        if (c == 'r') {
            trackballTransform = new Matrix4f().identity();
        }

        if (c == 'c') {
            if (cState == cameraState.ONSPIDER) {
                cState = cameraState.STATIONARY;

            } else if (cState == cameraState.STATIONARY) {
                cState = cameraState.ONSPIDER;
            }
        }

    }

    public void mousePressed(int x,int y)
    {
        mousePos = new Vector2f(x,y);
    }

    public void mouseReleased(int x,int y)
    {
        System.out.println("Released");
    }

    public void mouseDragged(int x,int y)
    {
        Vector2f newM = new Vector2f(x,y);

        Vector2f delta = new Vector2f(newM.x-mousePos.x,newM.y-mousePos.y);
        mousePos = new Vector2f(newM);

        trackballTransform = new Matrix4f().rotate(delta.x/trackballRadius,0,1,0)
                                           .rotate(delta.y/trackballRadius,1,0,0)
                                           .mul(trackballTransform);
    }

    public void reshape(GLAutoDrawable gla,int x,int y,int width,int height)
    {
        GL gl = gla.getGL();
        WINDOW_WIDTH = width;
        WINDOW_HEIGHT = height;
        gl.glViewport(0, 0, width, height);

//        projection = new Matrix4f().perspective((float)Math.toRadians(120.0f),(float)width/height,0.1f,10000.0f);
        projection = new Matrix4f().ortho(-400,400,-400,400,0.1f,10000.0f);

    }

    public void dispose(GLAutoDrawable gla)
    {
        GL3 gl = gla.getGL().getGL3();

    }



}
