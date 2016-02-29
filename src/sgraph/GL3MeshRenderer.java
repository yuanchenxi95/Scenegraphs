package sgraph;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.GLBuffers;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Map;

/**
 * This is a JOGL specific renderer for a mesh. It mandates OpenGL 3 and above. It encapsulates
 * all the JOGL-specific code required to render a mesh, namely a VAO, VBO, number and type of
 * primitives and the GL3 context required to call rendering functions.
 * {@link sgraph.GL3ScenegraphRenderer uses this implementation.}
 * @author Amit Shesh
 */
public class GL3MeshRenderer
{
    /**
     * The GL3 context used to call all OpenGL functions
     */
    private GL3 glContext;

    /**
     * Various IDs to supply position, normal, texture and index attributes for the mesh
     */
    enum Buffer_IDs {PositionArrayBuffer,NormalArrayBuffer,TexCoordArrayBuffer,IndexArrayBuffer,NumBuffers};
    //enum Buffer_IDs {PositionArrayBuffer,IndexArrayBuffer,NumBuffers};
    protected IntBuffer vao; //our VAO
    protected IntBuffer vbo;//all our Vertex Buffer Object IDs
    protected int primitive,primitiveCount;

    public GL3MeshRenderer()
    {
        vao = IntBuffer.allocate(1);
        vbo = IntBuffer.allocate(Buffer_IDs.NumBuffers.ordinal());
    }

    public void dispose()
    {
        glContext.glDeleteBuffers(Buffer_IDs.NumBuffers.ordinal(),vbo);
        glContext.glDeleteVertexArrays(1,vao);
    }


    public void setGL(GLAutoDrawable gla)
    {
        int a;
        glContext = gla.getGL().getGL3();
        glContext.glGenVertexArrays(1,vao);
        glContext.glBindVertexArray(vao.get(0));

        a = glContext.glGetError();
        glContext.glGenBuffers(Buffer_IDs.NumBuffers.ordinal(),vbo);
        a = glContext.glGetError();
    }

    public void draw()
    {
        //1. bind its VAO
        glContext.glBindVertexArray(vao.get(0));

        //2. execute the "superpower" command
        glContext.glDrawElements(primitive, primitiveCount, GL.GL_UNSIGNED_INT, 0);

        glContext.glBindVertexArray(0);
    }

    public void prepare(util.PolygonMesh mesh,Map<String,Integer> shaderVariableMap) throws Exception
    {
        int i, j;
        int a;

        if (glContext==null)
            throw new Exception("Context of the GL3MeshRenderer is not set");
        GL3 gl = glContext;

        FloatBuffer vertexBuffer = mesh.getVertexPositions();
        FloatBuffer normalBuffer = mesh.getVertexNormals();
        FloatBuffer texcoordsBuffer = mesh.getTexCoords();
        IntBuffer indexBuffer = mesh.getPrimitives();

        primitive = mesh.getPrimitiveType();
        primitiveCount = mesh.getPrimitiveCount();


		/*
		 *Bind the VAO as the current VAO, so that all subsequent commands affect it
		 */
        gl.glBindVertexArray(vao.get(0));
        a = gl.glGetError();

		/*
		 *Allocate the VBO for vertex data and send it to the GPU
		 */
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vbo.get(Buffer_IDs.PositionArrayBuffer.ordinal()));
        gl.glBufferData(GL3.GL_ARRAY_BUFFER, vertexBuffer.capacity() * GLBuffers.SIZEOF_FLOAT, vertexBuffer, GL3.GL_STATIC_DRAW);

        if (normalBuffer.capacity() > 0) {
            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vbo.get(Buffer_IDs.NormalArrayBuffer.ordinal()));
            gl.glBufferData(GL3.GL_ARRAY_BUFFER, normalBuffer.capacity() * GLBuffers.SIZEOF_FLOAT, normalBuffer, GL3.GL_STATIC_DRAW);
        }


        if (texcoordsBuffer.capacity() > 0)
        {
            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER,vbo.get(Buffer_IDs.TexCoordArrayBuffer.ordinal()));
            gl.glBufferData(GL3.GL_ARRAY_BUFFER, texcoordsBuffer.capacity()* GLBuffers.SIZEOF_FLOAT, texcoordsBuffer, GL3.GL_STATIC_DRAW);
        }



		/*
		 *Allocate the VBO for triangle indices and send it to GPU
		 */
        gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, vbo.get(Buffer_IDs.IndexArrayBuffer.ordinal()));
        gl.glBufferData(GL3.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity() * GLBuffers.SIZEOF_INT, indexBuffer, GL3.GL_STATIC_DRAW);



        int vPositionLocation=-1,vNormalLocation=-1,vTexCoordLocation=-1;

        if (shaderVariableMap.containsKey("vPosition"))
            vPositionLocation = shaderVariableMap.get("vPosition");
        if (shaderVariableMap.containsKey("vNormal"))
            vNormalLocation = shaderVariableMap.get("vNormal");
        if (shaderVariableMap.containsKey("vTexCoord"))
            vTexCoordLocation = shaderVariableMap.get("vTexCoord");
		/*
		 *Specify all the vertex attribute pointers, i.e. tell OpenGL how to organize data according to attributes rather than vertices
		 */

        if (vPositionLocation>=0)
        {
            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vbo.get(Buffer_IDs.PositionArrayBuffer.ordinal()));
            gl.glVertexAttribPointer(vPositionLocation, 4, GL3.GL_FLOAT, false, 0, 0);
            gl.glEnableVertexAttribArray(vPositionLocation);
        }


        if (vNormalLocation>=0)
        {
            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vbo.get(Buffer_IDs.NormalArrayBuffer.ordinal()));
            gl.glVertexAttribPointer(vNormalLocation, 4, GL3.GL_FLOAT, false, 0, 0);
            gl.glEnableVertexAttribArray(vNormalLocation);
        }

        if (vTexCoordLocation>=0)
        {
            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vbo.get(Buffer_IDs.TexCoordArrayBuffer.ordinal()));
            gl.glVertexAttribPointer(vTexCoordLocation, 4, GL3.GL_FLOAT, false, 0, 0);
            gl.glEnableVertexAttribArray(vTexCoordLocation);
        }



		/*
		 *Unbind the VAO to prevent accidental change to all the settings
		 *so at this point, this VAO has two VBOs and two enabled VertexAttribPointers.
		 * It is going to remember all of that!
		 */
        gl.glBindVertexArray(0);
    }

    public void cleanup()
    {
        if (vao.get(0)!=0)
        {
            //give back the VBO IDs to OpenGL, so that they can be reused
            glContext.glDeleteBuffers(Buffer_IDs.NumBuffers.ordinal(),vbo);
            //give back the VAO ID to OpenGL, so that it can be reused
            glContext.glDeleteVertexArrays(1,vao);
        }
    }

}
