package me.meerkat.towers.models;

import java.util.HashMap;

import org.bukkit.inventory.Inventory;

public class Holder {
	
	private Inventory inv;
	private boolean change, blocked;
	private int rank;
	private double budget;
	private HashMap<String, String> mapa;
	private String sequence;
	private Section section;


	public enum Section{
		Selector, Easy, Medium, Hard;
	}
	
	
	public Holder() {
		super();
		this.mapa = new HashMap<String, String>();
		this.change = false;
		this.section = Section.Selector;
		this.rank = 0;
		this.budget = 0;
		this.blocked = false;
	}
	
	public Inventory getInv() {
		return inv;
	}

	public void setInv(Inventory inv) {
		this.inv = inv;
	}

	
	public String getSequence() {
		return sequence;
	}

	
	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	
	public Section getSection() {
		return section;
	}

	public void setSection(Section section) {
		this.section = section;
	}

	public boolean isChange() {
		return change;
	}

	public void setChange(boolean change) {
		this.change = change;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank += rank;
	}
	
	public void resetRank() {
		this.rank = 0;
	}

	//Si Rank es -1 ha fallado
	public void failRank() {
		this.rank = -1;
	}
	
	public double getBudget() {
		return budget;
	}


	public void setBudget(double d) {
		this.budget = d;
	}

	public boolean getBlocked() {
		return blocked;
	}


	public void setBlocked( boolean d) {
		this.blocked = d;
	}


	public HashMap<String, String> getMapa() {
		return mapa;
	}


	public void setMapa(HashMap<String, String> mapa) {
		this.mapa = mapa;
	}
	
}
