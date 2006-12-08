package model;

public interface IPeople
{

    public abstract Float[] getChromosome();

    public abstract PeopleFeeling getFeeling();

    public abstract void setFeeling(PeopleFeeling feeling);

    public abstract IPeople getLovedPeople();

    public abstract void setLovedPeople(People lovedPeople);

    public static Integer GENE_LIFE = 0;

    public static Integer GENE_CURIOUS = 1;

    public static Integer GENE_SPEED = 2;

    public static Integer GENE_SENSIBILITY = 3;

    public static Integer GENE_FEAR = 4;

    public static Integer GENE_PROLIFIC = 5;

    public static Integer GENE_CANIBAL = 6;

    public static Integer GENE_CHARMING = 7;

    public static Integer GENE_DEFENCE = 8;
}