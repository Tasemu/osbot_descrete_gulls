package descrete_gulls;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.util.GraphicUtilities;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;
import org.osbot.rs07.utility.ConditionalSleep;

import java.awt.*;
import java.util.EnumSet;

@ScriptManifest(author = "Tasemu", info = "Kill seagulls at Port Sarim", name = "Descrete Gulls", version = 1.1, logo = "")
public class main extends Script {
	private Area docks = new Area(
	    new int[][]{
	        { 3026, 3241 },
	        { 3030, 3241 },
	        { 3030, 3238 },
	        { 3047, 3238 },
	        { 3047, 3234 },
	        { 3030, 3234 },
	        { 3030, 3211 },
	        { 3026, 3211 },
	        { 3026, 3216 },
	        { 3018, 3216 },
	        { 3018, 3220 },
	        { 3026, 3220 }
	    }
	);
	private NPC target;
	private enum State {
		WAIT,
		WALK,
		ATTACK
	};
	private EnumSet<Skill> skillToTrain = EnumSet.of(Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE, Skill.HITPOINTS);
	private long startTime;
	private int gullsKilled = 0;
	
	@Override
	public void onStart() {
		log("Welcome to Descrete Gulls.");
		log("version: " + getVersion());
		startTime = System.currentTimeMillis();
		
		for (Skill skill : skillToTrain) {
			getExperienceTracker().start(skill);
		}
	}

	private State getState() {
		NPC gull = getNpcs().closest("Seagull");
		
		if (target != null && myPlayer().isInteracting(target))
			return State.WAIT;
		
		if (!docks.contains(myPlayer()))
			return State.WALK;
		
		if (
			gull != null &&
			!gull.isUnderAttack() &&
			!gull.isHitBarVisible()
		)
			return State.ATTACK;
		
		return State.WAIT;
	}

	@Override
	public int onLoop() throws InterruptedException {
		switch (getState()) {
		case ATTACK:
			target = getNpcs().closest("Seagull");
			if (target != null) {
				if (target.interact("Attack")) {
					new ConditionalSleep(random(3000, 5000)) {
						@Override
						public boolean condition() throws InterruptedException {
							return target != null &&
								   target.getHealthPercent() > 0;
						}
					}.sleep();
				}
			}
			break;
		case WALK:
			getWalking().webWalk(docks);
			break;
		case WAIT:
			
			if (target != null && target.getHealthPercent() == 0) {
				this.gullsKilled++;
				this.target = null;
			}
			
			if (this.getDialogues().isPendingContinuation()) {
				this.getDialogues().clickContinue();
			}
			
			sleep(random(500, 700));
			break;
		}
		return random(200, 300);
	}

	@Override
	public void onExit() {
		log("Bye!");
	}

	@Override
	public void onPaint(Graphics2D g) {
		final long runTime = System.currentTimeMillis() - startTime;
		drawMouse(g);
		g.setColor(Color.WHITE);
		g.drawString("Descrete Gulls Public v" + this.getVersion(), 10, 40);
		g.drawString("Status:  " + getState().toString().toLowerCase() + "ing", 10, 55);
		g.drawString("Time running: " + formatTime(runTime), 10, 70);
		g.drawString("Gulls Wasted: " + this.gullsKilled, 10, 85);
		int trainingPaintMargin = 0;
		
		for (Skill skill : skillToTrain) {
			if (getExperienceTracker().getGainedXP(skill) > 0) {
				g.drawString(skill.toString().toLowerCase() + " xp: " + getExperienceTracker().getGainedXP(skill), 10, 100 + trainingPaintMargin);
				trainingPaintMargin += 15;
			}
		}
		
		if (target != null && target.exists()) {
			g.setColor(Color.RED);
			GraphicUtilities.drawWireframe(getBot(), g, target);
		}
	}
	
	public final String formatTime(final long ms){
        long s = ms / 1000, m = s / 60, h = m / 60;
        s %= 60; m %= 60; h %= 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }
    
    private void drawMouse(Graphics graphics) {
		((Graphics2D) graphics).setRenderingHints(
			new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
		Point pointer = mouse.getPosition();
		Graphics2D spinG = (Graphics2D) graphics.create();
		Graphics2D spinGRev = (Graphics2D) graphics.create();
		spinG.setColor(new Color(255, 255, 255));
		spinGRev.setColor(Color.cyan);
		spinG.rotate(System.currentTimeMillis() % 2000d / 2000d * (360d) * 2 * Math.PI / 180.0, pointer.x, pointer.y);
		spinGRev.rotate(System.currentTimeMillis() % 2000d / 2000d * (-360d) * 2 * Math.PI / 180.0, pointer.x, pointer.y);
		final int outerSize = 20;
		final int innerSize = 12;
		spinG.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		spinGRev.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		spinG.drawArc(pointer.x - (outerSize / 2), pointer.y - (outerSize / 2), outerSize, outerSize, 100, 75);
		spinG.drawArc(pointer.x - (outerSize / 2), pointer.y - (outerSize / 2), outerSize, outerSize, -100, 75);
		spinGRev.drawArc(pointer.x - (innerSize / 2), pointer.y - (innerSize / 2), innerSize, innerSize, 100, 75);
		spinGRev.drawArc(pointer.x - (innerSize / 2), pointer.y - (innerSize / 2), innerSize, innerSize, -100, 75);
	}

}
