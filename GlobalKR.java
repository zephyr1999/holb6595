package holb6595;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import spacesettlers.actions.AbstractAction;
import spacesettlers.objects.AbstractActionableObject;
import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Base;
import spacesettlers.objects.Beacon;
import spacesettlers.objects.Ship;
import spacesettlers.simulator.Toroidal2DPhysics;

public class GlobalKR
{
	// This knowledge representation will be for a global perspective
	public UUID myID;
	private Ship myShip;
	private Ship friendlyShip;
	private double myEnergy;
	private double friendlyEnergy;
	private Base myHomeBase;

	private ArrayList<Base> enemyHome = new ArrayList<Base>();
	private ArrayList<Base> enemyBase = new ArrayList<Base>();

	private ArrayList<Ship> enemyShipList = new ArrayList<Ship>();
	private ArrayList<Double> enemyEnergyList = new ArrayList<Double>();

	private ArrayList<Asteroid> resourceLocList = new ArrayList<Asteroid>();
	private ArrayList<Asteroid> asteroidLocList = new ArrayList<Asteroid>();
	private ArrayList<Asteroid> allAsteroidList = new ArrayList<Asteroid>();
	private ArrayList<Beacon> beaconList = new ArrayList<Beacon>();
	private Set<AbstractActionableObject> actionableSet = new HashSet<AbstractActionableObject>();

	private Ship nearestShip = null;
	private double distanceToNearestEnemy;
	private Asteroid nearestResource = null;
	private double distanceToNearResource;
	private Beacon nearestBeacon;
	private double distanceToNearBeacon;
	private final double MIN_BASE_DIST = 20;

	Toroidal2DPhysics mySpace;

	public boolean isGlobal = true;
	public ArrayList<AbstractAction> nextMoves = new ArrayList<AbstractAction>();
	public GlobalKR(Toroidal2DPhysics space, Ship ship)
	{
		mySpace = space;
		myID = ship.getId();
		update(space,ship);
	}

	//
	// Accessors
	Set<AbstractActionableObject> getActionableList()
	{
		return actionableSet;
	}
	ArrayList<Asteroid> getAllAsteroidList()
	{
		return allAsteroidList;
	}
 	double getMyEnergy()
	{
		return myEnergy;
	}
	Ship getMyShip()
	{
		return myShip;
	}
	Asteroid getNearResource()
	{
		return nearestResource;
	}
 	Base getHomeBase()
	{
		return myHomeBase;
	}

 	// End Accessors


 	//
 	// Other Methods
 	void update(Toroidal2DPhysics space, Ship ship)
	{
		// Set my position and energy
		myShip = ship;
		myEnergy = ship.getEnergy();

		// Set the position/energy/distance of all ships
		for(Ship othership : space.getShips())
		{
			// If the ship is ours add to our locations/energy
			// Else add it to the enemy locations
			if(othership.getTeamName().equals(ship.getTeamName()) && !(othership.getId().equals(ship.getId())))
			{
				// Add friendly positions to the lists
				friendlyShip = othership;
				friendlyEnergy = othership.getEnergy();
				// Add friendly ship to actionable list
				actionableSet.add(othership);
			}
			else
			{
				// Add ship to enemylist and add it's energy as well
				enemyShipList.add(othership);
				enemyEnergyList.add(othership.getEnergy());
				// Add this ship to actionable objectlist as well
				actionableSet.add(othership);

				// Set which ship is the closest to our own
				if(nearestShip == null)
				{
					nearestShip = othership;
					distanceToNearestEnemy = space.findShortestDistance(ship.getPosition(), othership.getPosition());
				}
				else
				{
					double distance = space.findShortestDistance(ship.getPosition(), othership.getPosition());
					if(distance < distanceToNearestEnemy)
					{
						nearestShip = othership;
						distanceToNearestEnemy = distance;
					}
				}
			}

			// Set the position of the asteroids/resources
			for(Asteroid asteroid : space.getAsteroids())
			{
				// No matter if the asteroid is mineable or not, add to allAsteroid list
				allAsteroidList.add(asteroid);

				// if it's mineable add it to resource list
				// else add to asteroid list
				if(asteroid.isMineable())
				{
					resourceLocList.add(asteroid);

					// Check if this resource is the closest to our ship.
					// If so update
					if(nearestResource == null)
					{
						nearestResource = asteroid;
						distanceToNearResource = space.findShortestDistance(ship.getPosition(), asteroid.getPosition());
					}
					else
					{
						double distance = space.findShortestDistance(ship.getPosition(), asteroid.getPosition());
						if(distance < distanceToNearResource)
						{
							nearestResource = asteroid;
							distanceToNearResource = distance;
						}
					}
				}
				else
					asteroidLocList.add(asteroid);
			}
		}

		// Set the position of the beacons
		for(Beacon beacon : space.getBeacons())
		{ // TODO BEACON
			// add beacon to beacon list
			beaconList.add(beacon);


			// Check if this beacon is the closest to our ship.
			// If so update
			if(nearestBeacon == null)
			{
				nearestBeacon = beacon;
				distanceToNearResource = space.findShortestDistance(ship.getPosition(), beacon.getPosition());
			}
			else
			{
				double distance = space.findShortestDistance(ship.getPosition(), beacon.getPosition());
				if(distance < distanceToNearResource)
				{
					nearestBeacon = beacon;
					distanceToNearResource = distance;
				}
			}
		}

		// Set base locations
		for(Base base : space.getBases())
		{
			// Add base to asteroid list
			actionableSet.add(base);

			// if base is ours set ourbase loc
			if(base.getTeamName().equals(ship.getTeamName()))
				myHomeBase = base;
			else if(!(base.isHomeBase()))
				enemyHome.add(base);
			else
				enemyBase.add(base);
		}
	}

	public boolean hasNextMove() {
		return nextMoves.size() > 0;
	}

	public AbstractAction nextMove() {
		AbstractAction move = nextMoves.get(0);
		nextMoves.remove(0);
		return move;
	}

	public void putMove(AbstractAction move) {
		nextMoves.add(move);

	}

	public boolean nextToBase()
	{
		return mySpace.findShortestDistance(myShip.getPosition(), myHomeBase.getPosition()) < MIN_BASE_DIST;
	}

	public void clearNextMoves()
	{
		nextMoves.clear();
	}

	public Beacon getNearBeacon()
	{
		return nearestBeacon;
	}
}
