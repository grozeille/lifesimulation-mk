package game;


import java.util.logging.Level;

import com.jme.app.VariableTimestepGame;
import com.jme.input.KeyInput;
import com.jme.renderer.ColorRGBA;
import com.jme.system.DisplaySystem;
import com.jme.system.JmeException;
import com.jme.util.LoggingSystem;
import com.jmex.game.state.GameStateManager;
import com.jmex.game.state.load.TransitionGameState;

public class Game extends VariableTimestepGame
{
    private IngameState ingameState;
    private MenuState menuState;    
    
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
        menuState = new MenuState("mnue");         
        GameStateManager.getInstance().attachChild(menuState);        
        menuState.setActive(true);        
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
            display.setTitle("Life simulation");            
        } catch (JmeException e)
        {
            e.printStackTrace();
            throw new RuntimeException(
                    "Erreur lors de l'initialisation du systéme", e);
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
        if(menuState.isActive() && menuState.isStartGame())
        {                       
            TransitionGameState transitionState = new TransitionGameState(null);
            GameStateManager.getInstance().attachChild(transitionState);                        
            ingameState = new IngameState("ingame", menuState.getNbPeople(), menuState.getRandomGene());
            GameStateManager.getInstance().attachChild(ingameState);
            menuState.setActive(false);
            //transitionState.setActive(true);
            ingameState.setActive(true);
        }
        else if(menuState.isActive() && menuState.isQuitGame())
        {
            finish();
        }
        // si le menu demande de quitter, on quite
        else if(ingameState != null && ingameState.isActive() && ingameState.isGameOver())
        {
            ingameState.setActive(false);
            GameStateManager.getInstance().detachChild(ingameState);
            ingameState = null;
            menuState.setActive(true);
        }
        else
        {
            GameStateManager.getInstance().update(deltaTime);
        }
    }

}
