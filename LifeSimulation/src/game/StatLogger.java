package game;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import model.People;

import com.jme.scene.Spatial;

public class StatLogger
{
    private StatLogger(){}
    
    private static StatLogger instance;
    public static StatLogger GetInstance()
    {
        if(instance == null)
            instance = new StatLogger();
        return instance;
    }
    
    private boolean enable = false;
    
    
    private ArrayList<Date> birthList = new ArrayList<Date>();
    
    public void LogBirth()
    {     
        if(enable)
            birthList.add(Calendar.getInstance().getTime());        
    }
    
    private HashMap<Date, DeathCause> deathList = new HashMap<Date, DeathCause>();
    
    public void LogDeath(DeathCause cause)
    {
        if(enable)
            deathList.put(Calendar.getInstance().getTime(), cause);
    }
    
    private HashMap<Date, Float[]> caracteristic = new HashMap<Date, Float[]>();
    
    public void LogPeople(ArrayList<Spatial> nodes)
    {
        if(!enable)
            return;
        // 1-life, 2-prolific, 3-charming, 4-fear, 5-defence,
        // 6-curious, 7-speed, 8-canibal, 9-sensibility, 10-nbPeople
        Float[] carac = new Float[]{0f,0f,0f,0f,0f,0f,0f,0f,0f,0f}; 
        Integer nbPeople = 0;
        for(Spatial s : nodes)
        {
            if(s instanceof People)
            {
                People p = (People)s;
                if(p != null)
                {
                    carac[0] += p.getChromosome()[People.GENE_LIFE];
                    carac[1] += p.getChromosome()[People.GENE_PROLIFIC];
                    carac[2] += p.getChromosome()[People.GENE_CHARMING];
                    carac[3] += p.getChromosome()[People.GENE_FEAR];
                    carac[4] += p.getChromosome()[People.GENE_DEFENCE];
                    carac[5] += p.getChromosome()[People.GENE_CURIOUS];
                    carac[6] += p.getChromosome()[People.GENE_SPEED];
                    carac[7] += p.getChromosome()[People.GENE_CANIBAL];
                    carac[8] += p.getChromosome()[People.GENE_SENSIBILITY];
                    nbPeople++;
                }
            }
        }
        carac[9] = nbPeople.floatValue();
        for(int cpt = 0; cpt < 9; cpt++)
        {
            carac[cpt] /= nbPeople;
        }
        caracteristic.put(Calendar.getInstance().getTime(), carac);
    }
    
    public void Save()
    {
        if(enable)
        {
            SaveBirthAndDeath();
            SaveDeathCause();
            SaveCaracteristic();
        }
    }
    
    private void SaveCaracteristic()
    {
        try
        {
            BufferedWriter bw = new BufferedWriter(new FileWriter("caracteristics.csv", false));
            bw.write("Time;Life;Prolific;Charming;Fear;Defence;Curious;Speed;Canibal;Sensibility;People\n");
            
            ArrayList<Date> dates = new ArrayList<Date>(caracteristic.keySet()); 
            Collections.sort(dates);
            
            for(Date date : dates)
            {
                Float[] carac = caracteristic.get(date);
                String result = date.toString();
                for(int cpt = 0; cpt < 10; cpt++)
                {
                    result +=";"+carac[cpt];
                }
                result+="\n";
                bw.write(result);
            }
            
            bw.close();
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void SaveDeathCause()
    {
        try
        {
            BufferedWriter bw = new BufferedWriter(new FileWriter("deathCause.csv", false));      
            bw.write("Cause;Number\n");
            
            Integer[] causes = new Integer[]{0,0,0,0};
            for(DeathCause cause : deathList.values())
            {
                if(cause == DeathCause.disease)
                    causes[0]++;
                else if(cause == DeathCause.eated)
                    causes[1]++;
                else if(cause == DeathCause.natural)
                    causes[2]++;
                else if(cause == DeathCause.sex)
                    causes[3]++;
            }
            
            bw.write("Disease;"+causes[0]+"\n");
            bw.write("Eated;"+causes[1]+"\n");
            bw.write("Natural;"+causes[2]+"\n");
            bw.write("Sex;"+causes[3]+"\n");
            
            bw.close();
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void SaveBirthAndDeath()
    {
        try
        {
            BufferedWriter bw = new BufferedWriter(new FileWriter("birthAndDeath.csv", false));
            bw.write("Time;Birth;Death\n");
            if(birthList.size() == 0)
                return;
            Date date = birthList.get(0);           
            Date now = Calendar.getInstance().getTime();
            Integer index = 10;
            
            while(date.before(now))
            {
                Integer birthNumber = 0;
                Integer deathNumber = 0;
                for(Date birthDate : birthList)
                {
                    if(birthDate.getTime() >= date.getTime() && 
                            birthDate.getTime() < date.getTime()+10000)
                    {
                        birthNumber++;
                    }
                }
                for(Date deathDate : deathList.keySet())
                {
                    if(deathDate.getTime() >= date.getTime() && 
                            deathDate.getTime() < date.getTime()+10000)
                    {
                        deathNumber++;
                    }
                }
                bw.write(index+";"+birthNumber+";"+deathNumber+"\n");
                index += 10;
                date.setTime(date.getTime()+10000);
            }            
            bw.close();
            
        } catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public boolean isEnable()
    {
        return enable;
    }

    public void setEnable(boolean enable)
    {
        this.enable = enable;
    }
}
