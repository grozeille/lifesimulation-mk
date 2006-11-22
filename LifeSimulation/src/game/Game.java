package game;


import java.util.logging.Level;

import com.jme.app.VariableTimestepGame;
import com.jme.input.KeyInput;
import com.jme.renderer.ColorRGBA;
import com.jme.system.DisplaySystem;
import com.jme.system.JmeException;
import com.jme.util.LoggingSystem;
import com.jmex.game.state.GameStateManager;

public class Game extends VariableTimestepGame
{
    private IngameState ingameState;
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        LoggingSystem.getLogger().setLevel(Level.OFF);
        Game app = new Game();
        app.start();
    }    
    
    @Override
    public void finish()
    {
        StatLogger.GetInstance().Save();
        super.finish();
    }
    
    @Override
    protected void cleanup()
    {
        GameStateManager.getInstance().cleanup();
        KeyInput.destroyIfInitalized();
    }

    @Override
    protected void initGame()
    {               
        ingameState = new IngameState("ingame");
        GameStateManager.getInstance().attachChild(ingameState);
        ingameState.setActive(true);
    }

    @Override
    protected void initSystem()
    {
        // initialise les variables de l'application (taille de l'�cran etc.)
        try
        {
            display = DisplaySystem.getDisplaySystem(properties.getRenderer());
            display.createWindow(properties.getWidth(), properties.getHeight(),
                    properties.getDepth(), properties.getFreq(), properties
                            .getFullscreen());
            display.getRenderer().setBackgroundColor(ColorRGBA.black);
            display.setTitle("SimpleSpaceQuake");            
        } catch (JmeException e)
        {
            e.printStackTrace();
            throw new RuntimeException(
                    "Erreur lors de l'initialisation du syst�me", e);
        }
        
        GameStateManager.create();
    }

    @Override
    protected void reinit()
    {
        // TODO Auto-generated method stub
    }

    @Override
    protected void render(float interpolation)
    {        
        GameStateManager.getInstance().render(interpolation);
    }

    @Override
    protected void update(float deltaTime)
    {       
        // si le menu demande de quitter, on quite
        if(ingameState.isGameOver())
            finish();
        else
        {
            GameStateManager.getInstance().update(deltaTime);
        }
    }

}
