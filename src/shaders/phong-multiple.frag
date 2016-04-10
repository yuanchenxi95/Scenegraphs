#version 400 core

struct MaterialProperties
{
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    float shininess;
};

struct LightProperties
{
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    vec4 position;
    vec4 spotDirection;
    float spotCutoff;
};



in vec3 fNormal;
in vec4 fPosition;
in vec4 fTexCoord;

const int MAXLIGHTS = 10;

uniform MaterialProperties material;
uniform LightProperties light[MAXLIGHTS];
uniform int numLights;

/* texture */
uniform sampler2D image;

out vec4 fColor;


void main()
{
    vec3 lightVec,viewVec,reflectVec,spotVec;
    vec3 normalView;
    vec3 ambient,diffuse,specular;
    float nDotL,rDotV;


    fColor = vec4(0,0,0,1);
	normalView = normalize(fNormal.xyz);
    viewVec = -fPosition.xyz;
    viewVec = normalize(viewVec);

    for (int i=0;i<numLights;i++)
    {
        if (light[i].position.w!=0)
            lightVec = normalize(light[i].position.xyz - fPosition.xyz);
        else
            lightVec = normalize(-light[i].position.xyz);

        vec3 tNormal = fNormal;
        nDotL = dot(normalView,lightVec);


        int s = 1;

        if (light[i].position.w!=0){
            if (dot(-lightVec,normalize(light[i].spotDirection.xyz)) > light[i].spotCutoff)
               s = 1;
            else
               s = 0;
        }

        reflectVec = reflect(-lightVec,normalView);
        reflectVec = normalize(reflectVec);

        rDotV = max(dot(reflectVec,viewVec),0.0);

        ambient = material.ambient * light[i].ambient;
        diffuse = material.diffuse * light[i].diffuse * max(nDotL,0);
        if (nDotL>0)
            specular = material.specular * light[i].specular * pow(rDotV,material.shininess);
        else
            specular = vec3(0,0,0);

        fColor = fColor + s * (vec4(ambient+diffuse+specular,1.0));
    }
    fColor *= texture(image,fTexCoord.st);

}


