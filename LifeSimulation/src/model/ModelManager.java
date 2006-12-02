package model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;

import com.jme.scene.Node;
import com.jme.util.export.binary.BinaryExporter;
import com.jme.util.export.binary.BinaryImporter;

/**
 * gestionnaire de modèle 3D,
 * permet de charger un modèle depuis un fichier 3DS, 
 * avec gestion d'un cache (afin d'�viter de charger le même fichier plusieurs fois)
 * @author mathias
 *
 */
public class ModelManager
{
    private Hashtable<String, byte[]> modelCache = new Hashtable<String, byte[]>();
    
    private ModelManager()
    {    
    }      
    
    static private ModelManager instance = null;
    /**
     * obtient le singleton
     * @return
     */
    static public ModelManager getInstance()
    {
        if(instance == null)
            instance = new ModelManager();
        return instance;
    }
    
    /**
     * obtient un Node � partir d'un modèle 3DS<br/>
     * TODO : faire avec <code>CloneImportExport</code>
     * @param name
     * @return
     * @throws IOException 
     */
    public Node loadModel(String name) throws IOException
    {
        Node result;
        // charge le modèle associé à l'objet depuis le cache ou le fichier
        if (modelCache.containsKey(name))
        {      
            result = (Node) new BinaryImporter().load(modelCache.get(name));
        } else
        {
            URL modelUrl = getClass().getResource(
                    "/ressources/" + name + ".jme");
            /*MaxToJme maxToJme = new MaxToJme();

            InputStream maxInputStream = modelUrl.openStream();
            ByteArrayOutputStream jmeOutputStream = new ByteArrayOutputStream();
            maxToJme.convert(maxInputStream, jmeOutputStream);
            maxInputStream.close();

            BinaryImporter bi = new BinaryImporter();
            byte[] modelData = jmeOutputStream.toByteArray();
            jmeOutputStream.close();

            result = (Node) bi.load(modelData);*/
            result = (Node)BinaryImporter.getInstance().load(modelUrl);
            
            // sauvegarde ce modèle en mémoire pour en plus aller le chercher sur le disque
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BinaryExporter.getInstance().save(result, outputStream);
            byte[] modelData = outputStream.toByteArray();
            
            modelCache.put(name, modelData);
        }
        
        return result;
    }
}
