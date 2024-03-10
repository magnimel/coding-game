import java.util.ArrayList;
import java.util.Scanner;

class Position {
    // Define position boundaries and goal center
    public static final int MIN_X = 0;
    public static final int MAX_X = 16000;
    public static final int MIN_Y = 0;
    public static final int MAX_Y = 7500;
    public static final int GOAL_CENTER_Y = 3750;

    // Coordinates of the position
    private int x;
    private int y;

    // Constructor
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    // Method to calculate the distance between two positions
    public static double distance(Position p1, Position p2) {
        int dx = p2.getX() - p1.getX();
        int dy = p2.getY() - p1.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Method to calculate the slope of a line between two positions
    public static double slope(Position p1, Position p2) {
        int dx = p2.getX() - p1.getX();
        int dy = p2.getY() - p1.getY();
        double slope = (double) dy / dx;
        return slope;
    }

}

// Class to represent a Unit in the game
class Unit {
    private int id;
    private String type;
    private int vx;
    private int vy;
    private int state;
    private Position position;

    // Method to update the state of the unit
    void updateUnit(int id, String type, int x, int y, int vx, int vy, int state) {
        this.id = id;
        this.type = type;
        this.position = new Position(x, y);
        this.vx = vx;
        this.vy = vy;
        this.state = state;
    }

    // Getter methods
    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public int getVy() {
        return vy;
    }

    public int getVx() {
        return vx;
    }

    public Position getPosition() {
        return position;
    }

    public int getState() {
        return state;
    }
}

// Class to represent a Spell Usage in the game
class SpellUsage {
    private String spell;
    private int targetId;
    private int remainingDuration;

    // Constructor
    public SpellUsage(String spell, int targetId, int duration) {
        this.spell = spell;
        this.targetId = targetId;
        this.remainingDuration = duration;
    }

    // Getter methods
    public String getSpell() {
        return spell;
    }

    public int getTargetId() {
        return targetId;
    }

    public int getRemainingDuration() {
        return remainingDuration;
    }

    // Method to decrement the duration of the spell
    public void decrementDuration() {
        remainingDuration--;
    }
}

// Class to represent a Spell in the game
class Spell {
    // Instance variables
    private boolean spellUsed = false;
    private ArrayList<SpellUsage> activeSpells = new ArrayList<>();

    // Method to reset the spell used flag
    public void resetSpellUsed() {
        this.spellUsed = false;
    }

    // Method to decrement the durations of all active spells
    public void decrementDurations() {
        for (SpellUsage spellUsage : activeSpells) {
            spellUsage.decrementDuration();
        }
        activeSpells.removeIf(spellUsage -> spellUsage.getRemainingDuration() <= 0);
    }

    // Method to use a spell
    public void useSpell(String spell, int id, int duration) {
        if (!spellUsed && !isSpellActiveOnTarget(spell, id)) {
            System.out.println(spell.toUpperCase() + " " + id);
            activeSpells.add(new SpellUsage(spell, id, duration));
            spellUsed = true;
        }
    }

    // Method to check if a spell is available to use on a target
    public boolean isSpellAvailable(String spell, int targetId) {
        return !spellUsed && !isSpellActiveOnTarget(spell, targetId);
    }

    // Helper method to check if a spell is active on a target
    private boolean isSpellActiveOnTarget(String spell, int targetId) {
        for (SpellUsage spellUsage : activeSpells) {
            if (spellUsage.getSpell().equals(spell) && spellUsage.getTargetId() == targetId) {
                return true;
            }
        }
        return false;
    }
}

// Class to represent a Player (my AI) in the game
class Player {
    // Instance variables
    private int teamId;
    private int wizard1Id;
    private int wizard2Id;
    private int opponentWizard1Id;
    private int opponentWizard2Id;
    private int bludger1Id;
    private int bludger2Id;
    Position myGoal;
    Position opponentGoal;
    private ArrayList<Integer> snaffleIds;
    private int snaffleCount;
    private int myScore;
    private int myMagic;
    private int opponentScore;
    private int opponentMagic;
    private int entities;
    private int wizard1TargetId = -1;
    private int wizard2TargetId = -1;
    private ArrayList<Unit> units;
    private Spell spell;
    Scanner in;

    // Constructor
    public Player() {
        in = new Scanner(System.in);
        this.teamId = in.nextInt();
        opponentGoal = (teamId == 0) ? new Position(Position.MAX_X, Position.GOAL_CENTER_Y)
                : new Position(Position.MIN_X, Position.GOAL_CENTER_Y);
        myGoal = (teamId == 0) ? new Position(Position.MIN_X, Position.GOAL_CENTER_Y)
                : new Position(Position.MAX_X, Position.GOAL_CENTER_Y);
        this.spell = new Spell();
    }

    // Method to get a unit by its ID
    private Unit getUnitById(int id) {
        for (Unit unit : units) {
            if (unit.getId() == id) {
                return unit;
            }
        }
        return null;
    }

    // Method to update the IDs of the units
    private void updateUnitIds() {
        int i = 0;
        int j = 0;
        int k = 0;
        snaffleIds = new ArrayList<>();
        snaffleCount = 0;
        for (Unit unit : units) {
            switch (unit.getType()) {
                case "WIZARD":
                    if (i == 0) {
                        wizard1Id = unit.getId();
                        i++;
                    } else {
                        wizard2Id = unit.getId();
                    }
                    break;
                case "OPPONENT_WIZARD":
                    if (j == 0) {
                        opponentWizard1Id = unit.getId();
                        j++;
                    } else {
                        opponentWizard2Id = unit.getId();
                    }
                    break;
                case "BLUDGER":
                    if (k == 0) {
                        bludger1Id = unit.getId();
                        k++;
                    } else {
                        bludger2Id = unit.getId();
                    }
                    break;
                case "SNAFFLE":
                    snaffleIds.add(unit.getId());
                    snaffleCount++;
                    break;
            }
        }
    }

    // Helper method to update game info
    private void updatePlayerDetails(int myScore, int myMagic, int opponentScore, int opponentMagic,
            int entities) {
        this.myScore = myScore;
        this.myMagic = myMagic;
        this.opponentScore = opponentScore;
        this.opponentMagic = opponentMagic;
        this.entities = entities;
    }

    // Method to update game info
    private void updateGame(Scanner in) {
        updatePlayerDetails(in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt());
        units = new ArrayList<Unit>();
        for (int i = 0; i < this.entities; i++) {
            Unit unit = new Unit();
            unit.updateUnit(in.nextInt(), in.next(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(),
                    in.nextInt());
            units.add(unit);
        }
        updateUnitIds();
        assignSnaffles();
    }

    // Method to print a move command
    private void printMove(Position position, int thrust) {
        System.out.println("MOVE " + position.getX() + " " + position.getY() + " " + thrust);
    }

    // Method to print a throw command
    private void printThrow(Position position, int power) {
        System.out.println("THROW " + position.getX() + " " + position.getY() + " " + power);
    }

    private void ajustViolence() {
        penaltyFactor += 33;
    }
    private double penaltyFactor = 150;
    // Method to assign Snaffles to the wizards
    private void assignSnaffles() {
        // Initialize the minimum total distance to the maximum possible value
        double minTotalDistance = Double.MAX_VALUE;

        // Initialize the IDs for the best snaffles for each wizard to -1
        int bestSnaffle1Id = -1;
        int bestSnaffle2Id = -1;

        // Get the wizard objects using their IDs
        Unit wizard1 = getUnitById(wizard1Id);
        Unit wizard2 = getUnitById(wizard2Id);

        // Define a penalty factor
         // New penalty factor value
        ajustViolence();
        // Iterate over each pair of snaffles
        for (Integer snaffle1Id : snaffleIds) {
            for (Integer snaffle2Id : snaffleIds) {
                // If there's more than one snaffle and the two wizards are assigned to the same
                // snaffle,
                // skip this combination
                if (snaffleCount > 1 && snaffle1Id == snaffle2Id) {
                    continue;
                }

                // Get the snaffle objects using their IDs
                Unit snaffle1 = getUnitById(snaffle1Id);
                Unit snaffle2 = getUnitById(snaffle2Id);

                // Calculate the distances from the wizards to their respective snaffles
                double distanceWizard1ToSnaffle1 = Position.distance(wizard1.getPosition(), snaffle1.getPosition());
                double distanceWizard2ToSnaffle2 = Position.distance(wizard2.getPosition(), snaffle2.getPosition());

                // Calculate the distances from the snaffles to my goal
                double distanceSnaffle1ToGoal = Position.distance(myGoal, snaffle1.getPosition());
                double distanceSnaffle2ToGoal = Position.distance(myGoal, snaffle2.getPosition());

                // Calculate the distance between the two wizards
                double distanceWizardToWizard = Position.distance(wizard1.getPosition(), wizard2.getPosition());

                // Calculate the average distance from the wizards to my goal
                double relativePosition1 = Position.distance(wizard1.getPosition(), myGoal);
                double relativePosition2 = Position.distance(wizard2.getPosition(), myGoal);
                double avg = (relativePosition1 + relativePosition2) / 2.0;

                // Calculate the penalties for the distances of the snaffles to the goal and the
                // wizards to each other
                double penaltySnaffle1 = penaltyFactor * Math.pow((distanceSnaffle1ToGoal / Position.MAX_X), 2);
                double penaltySnaffle2 = penaltyFactor * Math.pow((distanceSnaffle2ToGoal / Position.MAX_X), 2);
                double penaltyWizard = penaltyFactor * Math.pow((distanceWizardToWizard / avg), 2);

                // Calculate the total distance taking into account the penalties
                double totalDistance = (distanceWizard1ToSnaffle1 - penaltySnaffle1) +
                        (distanceWizard2ToSnaffle2 - penaltySnaffle2) +
                        (distanceWizardToWizard - penaltyWizard);

                // If the total distance for this combination of snaffles is less than the
                // current minimum, update the minimum and the best snaffle IDs
                if (totalDistance < minTotalDistance) {
                    minTotalDistance = totalDistance;
                    bestSnaffle1Id = snaffle1Id;
                    bestSnaffle2Id = snaffle2Id;
                }
            }
        }

        // Set the target IDs for the wizards to the IDs of the best snaffles
        wizard1TargetId = bestSnaffle1Id;
        wizard2TargetId = bestSnaffle2Id;
    }

    // Method to predict the position of a Snaffle after a certain number of turns
    private Position predictSnafflePosition(Unit snaffle, int turnsAhead) {
        // Get the current position and velocity of the Snaffle
        int x = snaffle.getPosition().getX();
        int y = snaffle.getPosition().getY();
        int vx = snaffle.getVx();
        int vy = snaffle.getVy();

        // Predict the snaffle's position after the given number of turns
        for (int i = 0; i < turnsAhead; i++) {
            x += vx * 0.75;
            y += vy * 0.75;

            // If the Snaffle would go past the left or right boundary,
            // reverse its x-velocity and set its x-position to the boundary
            if (x <= Position.MIN_X) {
                vx = -vx;
                x = Position.MIN_X;
            } else if (x >= Position.MAX_X) {
                vx = -vx;
                x = Position.MAX_X;
            }

            // If the Snaffle would go past the top or bottom boundary,
            // reverse its y-velocity and set its y-position to the boundary
            if (y <= Position.MIN_Y) {
                vy = -vy;
                y = Position.MIN_Y;
            } else if (y >= Position.MAX_Y) {
                vy = -vy;
                y = Position.MAX_Y;
            }
        }

        // Return the predicted position
        return new Position(x, y);
    }

    private int calculateTurnsAhead(Unit wizard, Unit snaffle) {
        // Calculate the distance from the wizard to the snaffle
        double wizardToSnaffleDistance = Position.distance(wizard.getPosition(), snaffle.getPosition());

        // Calculate the direction vector from the wizard to the snaffle
        double dx = snaffle.getPosition().getX() - wizard.getPosition().getX();
        double dy = snaffle.getPosition().getY() - wizard.getPosition().getY();

        // Normalize the direction vector
        double magnitude = Math.sqrt(dx * dx + dy * dy);
        double directionX = dx / magnitude;
        double directionY = dy / magnitude;

        // Calculate the thrust vector
        double thrustPower = 150.0; // constant for wizards
        double wizardMass = 1.0; // mass of the wizard
        double thrustX = directionX * thrustPower / wizardMass;
        double thrustY = directionY * thrustPower / wizardMass;

        // Calculate the new speed vector after thrust
        double newSpeedX = wizard.getVx() + thrustX;
        double newSpeedY = wizard.getVy() + thrustY;

        // Apply friction
        double friction = 0.75; // friction constant for wizards
        newSpeedX *= friction;
        newSpeedY *= friction;

        // Calculate the average speed
        double wizardAverageSpeed = Math.sqrt(newSpeedX * newSpeedX + newSpeedY * newSpeedY);

        // Define a distance threshold for using prediction
        double distanceThreshold = 1000;

        // Check if the snaffle is not moving or barely moving
        boolean isSnaffleMoving = (Math.abs(snaffle.getVx()) > 100) || (Math.abs(snaffle.getVy()) > 100);

        // Estimate the number of turns ahead
        int turnsAhead = 3; // move at less turns ahead
        if (wizardToSnaffleDistance > distanceThreshold && isSnaffleMoving) {
            turnsAhead = (int) (Math.ceil(wizardToSnaffleDistance / wizardAverageSpeed));
        }

        return turnsAhead;
    }

    // Method to move a wizard towards a target Snaffle
    private void grabSnaffle(Unit wizard, int wizardTargetId) {
        if (wizard.getState() == 0) { // If the wizard is not holding a Snaffle
            Unit targetSnaffle = getUnitById(wizardTargetId);
            if (targetSnaffle != null) { // If there is a target Snaffle
                // Calculate the number of turns ahead to predict the Snaffle's position
                int turnsAhead = calculateTurnsAhead(wizard, targetSnaffle);
                // Predict the Snaffle's future position
                Position futurePosition = predictSnafflePosition(targetSnaffle, turnsAhead);
                // Move towards the predicted position
                printMove(futurePosition, 150);
            } else {
                // If there is no target Snaffle, move towards my goal
                printMove(myGoal, 150);
            }
        }
    }

    // Method to calculate the adjusted zone based on the wizard's x-coordinate
    private int calculateAdjustedZone(int wizardX) {
        // Define the minimum and maximum of the zone
        int zoneMin = 0;
        int zoneMax = 3750;

        // The calculation is based on linear interpolation
        // The result depends on the team ID, which determines whether the goal is on
        // the left or right side

        // Calculate the adjusted zone by interpolating from the minimum to the maximum
        // zone
        // based on the wizard's x-coordinate relative to the minimum and maximum
        // x-positions

        if (teamId == 0) { // if your goal is on the left side
            return zoneMax - ((zoneMax - zoneMin) * (wizardX - Position.MIN_X) / (Position.MAX_X - Position.MIN_X));
        } else { // if your goal is on the right side
            return zoneMax - ((zoneMax - zoneMin) * (Position.MAX_X - wizardX) / (Position.MAX_X - Position.MIN_X));
        }
    }

    // Method to find the shortest path from a wizard to the goal within a given
    // zone
    private Position findShortestPathToGoal(Unit wizard, int zone) {

        // Calculate the minimum and maximum y-coordinates of the goal based on the
        // center y-coordinate and the zone
        int goalMinY = Position.GOAL_CENTER_Y - zone;
        int goalMaxY = Position.GOAL_CENTER_Y + zone;

        // Initialize the minimum distance to the maximum possible value
        double minDistance = Double.MAX_VALUE;
        // Initialize the best position to null
        Position bestPosition = null;

        // Iterate through each potential target point within the goal zone
        for (int i = goalMinY; i <= goalMaxY; i++) {
            // Depending on the team ID, the potential position is either at the maximum
            // x-coordinate or the minimum x-coordinate
            Position potentialPosition = (teamId == 0) ? new Position(Position.MAX_X, i)
                    : new Position(Position.MIN_X, i);

            // Calculate the distance from the wizard to the potential position
            double distance = Position.distance(wizard.getPosition(), potentialPosition);

            // If this distance is less than the current minimum distance, update the
            // minimum distance and the best position
            if (distance < minDistance) {
                minDistance = distance;
                bestPosition = potentialPosition;
            }
        }

        // If a best position was found, return it; otherwise, return the center
        // opponent's goal
        return (bestPosition != null) ? bestPosition : opponentGoal;
    }

    // Method to calculate the best throw position based on a given zone and the
    // wizard's position
    private Position calculateBestThrowPosition(Unit wizard, int zone) {
        int goalMinY = Position.GOAL_CENTER_Y - zone;
        int goalMaxY = Position.GOAL_CENTER_Y + zone;

        // Define a threshold for determining if a throw would be intercepted
        final double INTERCEPTION_THRESHOLD = 0.02; // slope

        // Initialize the best score to negative infinity
        double bestScore = Double.NEGATIVE_INFINITY;
        // Initialize the best position to null
        Position bestPosition = null;

        // Iterate through each potential target point within the goal zone
        for (int i = goalMinY; i <= goalMaxY; i += 10) {
            // Depending on the team ID, the potential position is either at the maximum
            // x-coordinate or the minimum x-coordinate
            Position potentialPosition = (teamId == 0) ? new Position(Position.MAX_X, i)
                    : new Position(Position.MIN_X, i);

            // Initialize the score for this potential position
            double score = 0;

            // Iterate through each unit to check for potential obstacles
            for (Unit unit : units) {
                if (unit.getId() != wizard.getId()
                        && ((teamId == 0 && wizard.getPosition().getX() < unit.getPosition().getX()) ||
                                (teamId == 1 && wizard.getPosition().getX() > unit.getPosition().getX()))) {

                    // Calculate the distance and slope to the unit
                    double distanceToUnit = Position.distance(wizard.getPosition(), unit.getPosition());
                    double slopeToUnit = Position.slope(wizard.getPosition(), unit.getPosition());
                    double slopeToPotentialPosition = Position.slope(wizard.getPosition(), potentialPosition);

                    // If the slope to the unit is close to the slope to the potential position,
                    // the throw could be intercepted by the unit
                    if (Math.abs(slopeToPotentialPosition - slopeToUnit) < INTERCEPTION_THRESHOLD) {

                        // Decrease or increase the score based on the type of unit and its distance
                        // These scores may be adjusted as needed
                        if (unit.getType().equals("BLUDGER")) {
                            score -= 500 / distanceToUnit; // You may adjust these values as needed
                        } else if (unit.getType().equals("OPPONENT_WIZARD")) {
                            score -= 700 / distanceToUnit;
                        } else if (unit.getType().equals("SNAFFLE")) {
                            score -= 200 / distanceToUnit;
                        } else if (unit.getType().equals("WIZARD")) { // This creates a kind of pass effect
                            score += 400 / distanceToUnit;
                        }
                    }
                }
            }

            // Increase the score based on the distance to the potential position
            score += 1000 / Position.distance(wizard.getPosition(), potentialPosition);

            // Update the best position if the score is higher
            if (score > bestScore) {
                bestScore = score;
                bestPosition = potentialPosition;
            }
        }

        // If a best position was found, return it; otherwise, calculate the shortest
        // path to the goal
        return (bestPosition != null) ? bestPosition : findShortestPathToGoal(wizard, zone);
    }

    // Method to throw a Snaffle
    private void throwSnaffle(Unit wizard) {
        if (wizard.getState() == 1) { // If the wizard is holding a Snaffle
            // Calculate the adjusted zone based on the wizard's x-coordinate
            int zone = calculateAdjustedZone(wizard.getPosition().getX());
            // Calculate the best position to throw the Snaffle towards
            Position bestThrowPosition = calculateBestThrowPosition(wizard, zone);
            // Print the command to throw the Snaffle towards the best position
            printThrow(bestThrowPosition, 500);
            // Reset the wizard's target after throwing the Snaffle
            if (wizard.getId() == wizard1Id) {
                wizard1TargetId = -1;
            } else {
                wizard2TargetId = -1;
            }
        }
    }

    // Method to determine if a spell should be used
    private boolean shouldUseSpell(Unit wizard) {
        return (wizard.getState() == 0) || (myScore == 2) || (opponentScore == 2)
                || (myScore == 3) || (opponentScore == 3);
    }

    // Method to determine if the Obliviate spell should be used
    private int shouldUseObliviate(Unit wizard) {
        // If no bludger met the conditions for casting the spell, return -1
        // Obliviate is useless, i am not using it ! ah ah
        return -1;
    }

    // Method to determine if the Petrificus spell should be used
    private int shouldUsePetrificus(Unit wizard) {
        // Determine the x-coordinate of my goal
        int goalX = myGoal.getX();

        // Check each Snaffle to see if it's moving towards my goal
        for (int snaffleId : snaffleIds) {
            Unit snaffle = getUnitById(snaffleId);
            if (teamId == 0) {
                if (snaffle.getPosition().getX() >= goalX
                        && snaffle.getPosition().getX() + 2 * snaffle.getVx() <= goalX)
                    return snaffleId;
            } else {
                if (snaffle.getPosition().getX() <= goalX
                        && snaffle.getPosition().getX() + 2 * snaffle.getVx() >= goalX)
                    return snaffleId;
            }
        }

        // If no suitable Snaffle was found, return -1
        return -1;
    }

    // Method to determine if the Accio spell should be used
    private int shouldUseAccio(Unit wizard) {
        // Define some constants for determining if a Snaffle is a suitable target
        final double OPPONENT_FIELD_PENETRATION_SNAFFLE = 0.0; // Customize this value as needed
        final double MIN_DISTANCE_SNAFFLE = Position.MAX_X * 0.0; // Customize this value as needed
        final double MAX_DISTANCE_SNAFFLE = Position.MAX_X * 0.65; // Customize this value as needed
        final int OPPONENT_FIELD_BOUNDARY_SNAFFLE = (teamId == 0)
                ? (int) (Position.MAX_X * OPPONENT_FIELD_PENETRATION_SNAFFLE)
                : (int) (Position.MAX_X * (1 - OPPONENT_FIELD_PENETRATION_SNAFFLE));
        final double DISTANCE_TOLERANCE = 0.9; // Slope difference threshold for interception

        int bestSnaffletoAccio = -1;
        double closestSnaffle = Double.POSITIVE_INFINITY;

        // If the wizard is on the opponent's side of the field
        if ((teamId == 0 && wizard.getPosition().getX() > OPPONENT_FIELD_BOUNDARY_SNAFFLE) ||
                (teamId == 1 && wizard.getPosition().getX() < OPPONENT_FIELD_BOUNDARY_SNAFFLE)) {
            // Check each Snaffle to see if it's a suitable target
            for (int snaffleId : snaffleIds) {
                Unit snaffle = getUnitById(snaffleId);
                double distanceToSnaffle = Position.distance(wizard.getPosition(), snaffle.getPosition());

                if (snaffleCount <= 2 &&
                        ((teamId == 0 && snaffle.getPosition().getX() < wizard.getPosition().getX()) ||
                                (teamId == 1 && snaffle.getPosition().getX() > wizard.getPosition().getX()))) {
                                    return snaffleId;
                                }
                // Skip this Snaffle if it's the target of the other wizard
                if ((wizard.getId() == wizard1Id && snaffleId == wizard2TargetId) ||
                        (wizard.getId() == wizard2Id && snaffleId == wizard1TargetId)) {
                    continue;
                }

                // If the Snaffle is behind the wizard and within the specified distance range
                if (distanceToSnaffle >= MIN_DISTANCE_SNAFFLE && distanceToSnaffle <= MAX_DISTANCE_SNAFFLE &&
                        ((teamId == 0 && snaffle.getPosition().getX() < wizard.getPosition().getX()) ||
                                (teamId == 1 && snaffle.getPosition().getX() > wizard.getPosition().getX()))) {

                    // Check the positions of the opponent wizards
                    Unit opponentWizard1 = getUnitById(opponentWizard1Id);
                    Unit opponentWizard2 = getUnitById(opponentWizard2Id);

                    // Calculate the slopes to the Snaffle and to each opponent wizard
                    double distanceToWizard = Position.distance(wizard.getPosition(), snaffle.getPosition());
                    // Calculate the distances to each opponent wizard
                    double wizardToOpponent1 = Position.distance(wizard.getPosition(), opponentWizard1.getPosition());
                    double wizardToOpponent2 = Position.distance(wizard.getPosition(), opponentWizard2.getPosition());
                    double distanceToOpponent1 = Position.distance(opponentWizard1.getPosition(),
                            snaffle.getPosition());
                    double distanceToOpponent2 = Position.distance(opponentWizard2.getPosition(),
                            snaffle.getPosition());

                    double distanceToOpponent = (wizardToOpponent1 < wizardToOpponent2) ? distanceToOpponent1
                            : distanceToOpponent2;
                    int opponentId = (wizardToOpponent1 < wizardToOpponent2) ? opponentWizard1Id : opponentWizard2Id;

                    Unit opponent = getUnitById(opponentId);

                    // If neither opponent wizard is closer to the snaffle than my wizard, the
                    // Snaffle is a suitable target
                    if ((distanceToWizard - distanceToOpponent) / distanceToWizard > DISTANCE_TOLERANCE
                            && ((teamId == 0 && opponent.getPosition().getX() < wizard.getPosition().getX()) ||
                                    (teamId == 1 && opponent.getPosition().getX() > wizard.getPosition().getX()))) {

                        if (distanceToWizard < closestSnaffle) {
                            bestSnaffletoAccio = snaffleId;
                        }
                    }

                    
                }
            }
            return bestSnaffletoAccio;
        }

        // If no suitable target was found, return -1
        return -1;
    }

    private double FlipendoTrajectory(Unit wizard, Unit snaffle) {
        Position wizardPosition = wizard.getPosition();
        Position snafflePosition = snaffle.getPosition();

        // Calculate the slope of the line connecting the wizard and the snaffle
        double slope = Position.slope(wizardPosition, snafflePosition);

        // Calculate the y-intercept (b) using the equation of a line: y = mx + b
        double intercept = wizardPosition.getY() - slope * wizardPosition.getX();

        // Determine the x-coordinate of the goal based on the team ID
        double goalX = (teamId == 0) ? Position.MAX_X : Position.MIN_X;

        // Calculate the y-coordinate of the line when it reaches the goal x
        double goalY = slope * goalX + intercept;

        // Check for wall rebounds and adjust goalY
        int rebounds = 0;
        while ((goalY < Position.MIN_Y || goalY > Position.MAX_Y) && rebounds < 1) {
            if (goalY < Position.MIN_Y) {
                // The snaffle hits the bottom wall and rebounds upwards
                intercept = -goalY;
                slope = -slope;
            } else if (goalY > Position.MAX_Y) {
                // The snaffle hits the top wall and rebounds downwards
                intercept = 2 * Position.MAX_Y - goalY;
                slope = -slope;
            }
            // Recalculate goalY with the new slope and intercept
            goalY = slope * goalX + intercept;

            // Increment the number of rebounds
            rebounds++;
        }

        return goalY;
    }

    private double DefensiveFlipendoTrajectory(Unit wizard, Unit snaffle) {
        Position wizardPosition = wizard.getPosition();
        Position snafflePosition = snaffle.getPosition();

        // Calculate the slope of the line connecting the wizard and the snaffle
        double slope = Position.slope(wizardPosition, snafflePosition);

        // Calculate the y-intercept (b) using the equation of a line: y = mx + b
        double intercept = wizardPosition.getY() - slope * wizardPosition.getX();

        // Determine the x-coordinate of our own goal based on the team ID
        double goalX = (teamId == 0) ? Position.MIN_X : Position.MAX_X;

        // Calculate the y-coordinate of the line when it reaches our own goal x
        double goalY = slope * goalX + intercept;

        // Check for wall rebounds and adjust goalY
        int rebounds = 0;
        while ((goalY < Position.MIN_Y || goalY > Position.MAX_Y) && rebounds < 1) {
            if (goalY < Position.MIN_Y) {
                // The snaffle hits the bottom wall and rebounds upwards
                intercept = -goalY;
                slope = -slope;
            } else if (goalY > Position.MAX_Y) {
                // The snaffle hits the top wall and rebounds downwards
                intercept = 2 * Position.MAX_Y - goalY;
                slope = -slope;
            }
            // Recalculate goalY with the new slope and intercept
            goalY = slope * goalX + intercept;

            // Increment the number of rebounds
            rebounds++;
        }

        return goalY;
    }

    // Method to determine if the Flipendo spell should be used
    private int shouldUseFlipendo(Unit wizard) {
        // Define some constants for determining if a snaffle is a suitable target
        final int ZONE = 1700; // Customize this value as needed
        final double SNAFFLE_MIN_DISTANCE_TO_GOAL = Position.MAX_X * 0.1; // Customize this value as needed
        final double MIN_DISTANCE_TO_SNAFFLE = Position.MAX_X * 0.1; // Customize this value as needed
        final double MAX_DISTANCE_TO_SNAFFLE = Position.MAX_X * 0.6; // Customize this value as needed

        // Calculate the minimum and maximum y-coordinates of the goal zone
        int goalMinY = Position.GOAL_CENTER_Y - ZONE;
        int goalMaxY = Position.GOAL_CENTER_Y + ZONE;

        // Initialize the ID of the best snaffle to -1
        int bestSnaffleId = -1;

        // Check each snaffle to see if it's a suitable target
        for (int snaffleId : snaffleIds) {
            Unit snaffle = getUnitById(snaffleId);
            double distanceToSnaffle = Position.distance(wizard.getPosition(), snaffle.getPosition());
            double distanceToGoal = Position.distance(snaffle.getPosition(), opponentGoal);

            // If the snaffle is within the specified distance range from the wizard,
            // beyond the minimum distance from the goal, and in front of the wizard
            if (MIN_DISTANCE_TO_SNAFFLE < distanceToSnaffle
                    && distanceToSnaffle < MAX_DISTANCE_TO_SNAFFLE
                    && distanceToGoal > SNAFFLE_MIN_DISTANCE_TO_GOAL
                    && ((teamId == 0 && (wizard.getPosition().getX() < snaffle.getPosition().getX())) ||
                            (teamId == 1 && (wizard.getPosition().getX() > snaffle.getPosition().getX())))) {

                // Calculate the y-coordinate of the line of the Flipendo trajectory at the goal
                double goalY = FlipendoTrajectory(wizard, snaffle);

                // If the y-coordinate of the line falls within the goal zone, the snaffle is a
                // suitable target
                if (goalY >= goalMinY && goalY <= goalMaxY) {
                    bestSnaffleId = snaffleId;
                }
            }
        }

        // Return the ID of the best snaffle, or -1 if no suitable target was found
        return bestSnaffleId;
    }

    private int shouldUseDefensiveFlipendo(Unit wizard) {
        // Define some constants for determining if a snaffle is a suitable target
        final int ZONE = 2300; // Customize this value as needed
        final double SNAFFLE_MIN_DISTANCE_TO_GOAL = Position.MAX_X * 0.35; // Customize this value as needed
        final double MIN_DISTANCE_TO_SNAFFLE = Position.MAX_X * 0.0; // Customize this value as needed
        final double MAX_DISTANCE_TO_SNAFFLE = Position.MAX_X * 0.4; // Customize this value as needed

        // Calculate the minimum and maximum y-coordinates of our own goal zone
        int myGoalMinY = Position.GOAL_CENTER_Y - ZONE;
        int myGoalMaxY = Position.GOAL_CENTER_Y + ZONE;

        // Initialize the ID of the best snaffle to -1
        int bestSnaffleId = -1;

        // Check each snaffle to see if it's a suitable target
        for (int snaffleId : snaffleIds) {
            Unit snaffle = getUnitById(snaffleId);
            double distanceToSnaffle = Position.distance(wizard.getPosition(), snaffle.getPosition());
            double distanceToGoal = Position.distance(snaffle.getPosition(), myGoal);

            // If the snaffle is moving towards our goal (i.e., its x-velocity is directed
            // towards our goal)
            boolean isMovingTowardGoal = (teamId == 0) ? snaffle.getVx() < -150 : snaffle.getVx() > 150;

            // If the snaffle is within the specified distance range from the wizard,
            // and behind the wizard relative to our own goal, and it's moving towards our
            // goal
            if (MIN_DISTANCE_TO_SNAFFLE < distanceToSnaffle
                    && distanceToSnaffle < MAX_DISTANCE_TO_SNAFFLE
                    && distanceToGoal < SNAFFLE_MIN_DISTANCE_TO_GOAL
                    && isMovingTowardGoal
                    && ((teamId == 0 && (wizard.getPosition().getX() > snaffle.getPosition().getX())) ||
                            (teamId == 1 && (wizard.getPosition().getX() < snaffle.getPosition().getX())))) {

                // Calculate the y-coordinate of the line of the Flipendo trajectory at the goal
                double goalY = DefensiveFlipendoTrajectory(wizard, snaffle);

                // If the y-coordinate of the line does not fall within our own goal zone, the
                // snaffle is a suitable target
                if (goalY < myGoalMinY || goalY > myGoalMaxY) {
                    bestSnaffleId = snaffleId;
                }
            }
        }

        // Return the ID of the best snaffle, or -1 if no suitable target was found
        return bestSnaffleId;
    }

    // Method to handle a wizard scenario
    private void scenario(Unit wizard) {
        // First, check if we should use a spell, or if we should use the Flipendo spell
        if (shouldUseSpell(wizard) || shouldUseFlipendo(wizard) != -1) {
            // Check if we should use the Obliviate spell
            int targetId = shouldUseObliviate(wizard);
            if (targetId != -1 && myMagic >= 5 && spell.isSpellAvailable("Obliviate", targetId)) {
                spell.useSpell("Obliviate", targetId, 4);
                return;
            }

            // Check if we should use the Flipendo spell
            targetId = shouldUseFlipendo(wizard);
            if (targetId != -1 && myMagic >= 20 && spell.isSpellAvailable("Flipendo", targetId)) {
                spell.useSpell("Flipendo", targetId, 1);
                return;
            }

            // Check if we should use the Accio spell
            targetId = shouldUseAccio(wizard);
            if (targetId != -1 && myMagic >= 15 && spell.isSpellAvailable("Accio", targetId)) {
                spell.useSpell("Accio", targetId, 6);
                return;
            }

            // // Check if we should use the Flipendo spell
            // targetId = shouldUseDefensiveFlipendo(wizard);
            // if (targetId != -1 && myMagic >= 20 && spell.isSpellAvailable("Flipendo", targetId)) {
            //     spell.useSpell("Flipendo", targetId, 1);
            //     return;
            // }

            // Check if we should use the Petrificus spell
            targetId = shouldUsePetrificus(wizard);
            if (targetId != -1 && myMagic >= 21 && spell.isSpellAvailable("Petrificus", targetId)) {
                spell.useSpell("Petrificus", targetId, 1);
                return;
            }

        }

        // If we didn't use a spell, then we can either move or throw
        if (wizard.getState() == 0) {
            // If the wizard is not holding a Snaffle, move to grab one
            int wizardTargetId = (wizard.getId() == wizard1Id) ? wizard1TargetId : wizard2TargetId;
            grabSnaffle(wizard, wizardTargetId);
        } else {
            // If the wizard is holding a Snaffle, throw it
            throwSnaffle(wizard);
        }
    }

    // Method to play
    public void play() {
        // Update the game info
        updateGame(in);

        // Main loop to play the game
        while (true) {
            // Decrement the durations of the spells and reset the spells used
            spell.decrementDurations();
            spell.resetSpellUsed();

            // Get the wizards
            Unit wizard1 = getUnitById(wizard1Id);
            Unit wizard2 = getUnitById(wizard2Id);

            // Handle the current scenario for each wizard
            scenario(wizard1);
            scenario(wizard2);

            // Update the game state
            updateGame(in);
        }
    }

    // main method
    public static void main(String[] args) {
        Player game = new Player();
        game.play();
    }
}