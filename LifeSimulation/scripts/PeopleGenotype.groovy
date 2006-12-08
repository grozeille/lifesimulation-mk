package scripts;

import model.IPeople;
import model.IPeopleGenotype;

public class PeopleGenotype implements IPeopleGenotype
{   
	public float GetSpeed(IPeople people, float time)
	{
	    return (((people.getChromosome()[IPeople.GENE_SPEED]+10f)/2f) * 
	             (people.getChromosome()[IPeople.GENE_CURIOUS]/100f))/2f * time*100f;
	    //return 0;
	}
}