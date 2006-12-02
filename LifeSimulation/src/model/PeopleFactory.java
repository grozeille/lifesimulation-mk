package model;

import java.io.IOException;
import java.util.Random;

public class PeopleFactory extends Thread
{
    public static float GESTATION_COUNTDOWN_MAX = 6f; // 6 secondes de gestation
    
    private People dad;
    private People mom;    
    
    private People child = null;
    public synchronized People getChild()
    {
        return child;
    }
    
    public PeopleFactory(People dad, People mom)
    {
        this.dad = dad;
        this.mom = mom;
    }
    
    @Override
    public void run()
    {        
        Float[] newChromosome = new Float[9];
        for(int cpt = 0; cpt < 9; cpt++)
        {
            float value1 = dad.getChromosome()[cpt];
            float value2 = mom.getChromosome()[cpt];
            
            float strong = value1>value2?value1:value2;
            float weak = value1<value2?value1:value2;
            newChromosome[cpt] = 0f;
            
            // 50% de chance de prendre le fort
            // 30% de prendre le faible
            // 20% de prendre entre les deux
            Random random = new Random();
            float randomResult = random.nextFloat();
            if(randomResult <= 0.7)
                newChromosome[cpt] = strong;
            else if(randomResult <= 0.9)
                newChromosome[cpt] = (strong + weak) / 2f;
            else
                newChromosome[cpt] = weak;
            
            // détermine la possibilité de muter
            if(random.nextFloat() <= 0.01f)
            {
                // ça peut muter en - ou en + avec 3 de max
                randomResult = random.nextFloat();
                if(randomResult <= 3f/10f)
                    newChromosome[cpt] += 3;
                else if(randomResult <= 5f/10f)
                    newChromosome[cpt] += 2;
                else if(randomResult <= 7f/10f)
                    newChromosome[cpt] += 1;
                else if(randomResult <= 8f/10f)
                    newChromosome[cpt] -= 1;
                else if(randomResult <= 9f/10f)
                    newChromosome[cpt] -= 2;
                else
                    newChromosome[cpt] -= 3;
            }
        }
        
        People child = null;
        try
        {
            child = new People(newChromosome);            
            
            float elapsedTime = 0;
            float maxTime = PeopleFactory.GESTATION_COUNTDOWN_MAX*
                100*child.getChromosome()[People.GENE_LIFE];
            // le temps de gestation dépend des points de vie de l'enfant            
            while(elapsedTime < maxTime)
            {
                elapsedTime += 10;
                Thread.sleep(10);
            }
            
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }    
        
        this.child = child;
       
        super.run();
    }
}
