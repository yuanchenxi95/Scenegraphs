package util;

import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * Created by ashesh on 9/24/2015.
 *
 * This class encapsulates in a very basic way, a GLSL shader program
 * Go through this code to see how to initialize and use a shader without using any JOGL classes.
 * This is the "barebones" way of working with GLSL shaders.
 */

public class ShaderProgram {
    //the GLSL program id
    private int program;
    private ShaderInfo []shaders;
    private boolean enabled;


    public ShaderProgram()
    {
        program = -1;
        shaders = new ShaderInfo[2];
        enabled = false;
    }

    public int getProgram()
    {
        return program;
    }

    public void createProgram(GL3 gl,String vertShaderFile,String fragShaderFile) throws FileNotFoundException, Exception
    {
        releaseShaders(gl);



        shaders[0] = new ShaderInfo(GL3.GL_VERTEX_SHADER,vertShaderFile,-1);
        shaders[1] = new ShaderInfo(GL3.GL_FRAGMENT_SHADER,fragShaderFile,-1);

        program = createShaders(gl);
    }

    public void releaseShaders(GL3 gl)
    {
        for (int i=0;i<shaders.length;i++)
        {
            if ((shaders[i]!=null) && (shaders[i].shader!=0))
            {
                gl.glDeleteShader(shaders[i].shader);
                shaders[i].shader = 0;
            }
        }
        if (program!=0)
            gl.glDeleteProgram(program);
        program = 0;
    }

    public void enable(GL3 gl)
    {
        gl.glUseProgram(program);
        enabled = true;
    }

    public void disable(GL3 gl)
    {
        gl.glUseProgram(0);
        enabled = false;
    }

    public int getUniformLocation(GL3 gl,String name)
    {
        boolean enabledStatus = enabled;

        if (!enabledStatus) //if not enabled, enable it
            enable(gl);

        int id = gl.glGetUniformLocation(program,name);

        if (!enabledStatus) //if it was not enabled, disable it again
            disable(gl);

        return id;
    }

    public Map<String,Integer> getAllShaderVariables(GL3 gl)
    {
        Map<String,Integer> map = new TreeMap<String,Integer>();
        IntBuffer numVars = IntBuffer.allocate(1);

        gl.glGetProgramiv(program, GL3.GL_ACTIVE_UNIFORMS,numVars);
        for (int i=0;i<numVars.get(0);i++)
        {
            IntBuffer length = IntBuffer.allocate(1);
            ByteBuffer nameVar = ByteBuffer.allocate(80);
            gl.glGetActiveUniformName(program,i,80,length,nameVar);
            String name = new String(nameVar.array(),0,length.get(0));
            int value = gl.glGetUniformLocation(program,name);
            map.put(name,value);
        }

        gl.glGetProgramiv(program, GL3.GL_ACTIVE_ATTRIBUTES,numVars);
        for (int i=0;i<numVars.get(0);i++)
        {
            IntBuffer length = IntBuffer.allocate(1);
            IntBuffer size = IntBuffer.allocate(1);
            IntBuffer type = IntBuffer.allocate(1);
            ByteBuffer nameVar = ByteBuffer.allocate(80);
            gl.glGetActiveAttrib(program,i,80,length,size,type,nameVar);
            String name = new String(nameVar.array(),0,length.get(0));
            int value = gl.glGetAttribLocation(program,name);
            map.put(name,value);
        }
        return map;

    }

    public int getAttributeLocation(GL3 gl,String name)
    {
        boolean enabledStatus = enabled;

        if (!enabledStatus) //if not enabled, enable it
            enable(gl);

        int id = gl.glGetAttribLocation(program,name);

        if (!enabledStatus) //if it was not enabled, disable it again
            disable(gl);

        return id;
    }

    private int createShaders(GL3 gl) throws FileNotFoundException, Exception
    {
        Scanner file;
        int shaderProgram;
        IntBuffer linked;


        shaderProgram = gl.glCreateProgram();


        for (int i=0;i<shaders.length;i++)
        {
            //    try
            {
                //file = new Scanner(new FileInputStream(shaders[i].filename));
                file = new Scanner(getClass().getClassLoader().getResourceAsStream(shaders[i].filename));
            }
       /*    catch (FileNotFoundException e)
            {
                throw new Exception("Could not open file " + shaders[i].filename);
            }
*/
            IntBuffer compiled = IntBuffer.allocate(1);



            String source,line;

            source = "";

            while (file.hasNext())
            {
                line = file.nextLine();
                source = source + "\n" + line;
            }
            file.close();


            //const char *codev = source.c_str();


            shaders[i].shader = gl.glCreateShader(shaders[i].type);
            gl.glShaderSource(shaders[i].shader, 1, new String[]{source},null);
            gl.glCompileShader(shaders[i].shader);
            gl.glGetShaderiv(shaders[i].shader, GL3.GL_COMPILE_STATUS,compiled);

            if (compiled.get(0)!=1)
            {
                int infologLen = 0;
                int charsWritten = 0;
                IntBuffer infoLogLen = IntBuffer.allocate(1);

                gl.glGetShaderiv(shaders[i].shader,GL3.GL_INFO_LOG_LENGTH,infoLogLen);
                int size = infoLogLen.get(0);
                //	printOpenGLError();
                if (size>0)
                {
                    ByteBuffer infoLog = ByteBuffer.allocate(size);
                    {
                        gl.glGetShaderInfoLog(shaders[i].shader,size,infoLogLen,infoLog);
                        System.err.print(infoLog);
                    }
                }
                releaseShaders(gl);

            }
            gl.glAttachShader(shaderProgram, shaders[i].shader);
        }

        linked = IntBuffer.allocate(1);;
        gl.glLinkProgram(shaderProgram);
        gl.glGetProgramiv(shaderProgram, GL3.GL_LINK_STATUS, linked);

        if (linked.get(0)!=1)
        {

            return 0;
        }


        return shaderProgram;
    }

    class ShaderInfo
    {
        int       type; //is it a vertex shader, a fragment shader, a geometry shader, a tesselation shader or none of the above?
        String  filename; //the file that stores this shader
        int       shader; //the ID for this shader after it has been compiled

        public ShaderInfo(int type,String filename,int shader)
        {
            this.type = type;
            this.filename = filename;
            this.shader = shader;
        }
    }


}
