/*
 * This file is part of Spoutcraft (http://wiki.getspout.org/).
 * 
 * Spoutcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Spoutcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.getspout.spout.gui.settings;

import java.util.UUID;

import net.minecraft.client.Minecraft;

import org.getspout.spout.config.ConfigReader;
import org.spoutcraft.spoutcraftapi.event.screen.ButtonClickEvent;
import org.spoutcraft.spoutcraftapi.gui.GenericCheckBox;

public class BiomeColorsButton extends GenericCheckBox{
	UUID fancyGraphics;
	public BiomeColorsButton(UUID fancyGraphics) {
		super("Fancy Biome Colors");
		this.fancyGraphics = fancyGraphics;
		this.setChecked(ConfigReader.fancyBiomeColors);
		setTooltip("Biome Colors\nFast - caches colors for grass and water per chunk.\nMay cause sharp changes in color near chunk edges.\nFancy - normal coloring for grass and water.\nCalculates color for water and grass for each block.");
	}
	
	@Override
	public void onButtonClick(ButtonClickEvent event) {
		ConfigReader.fancyBiomeColors = !ConfigReader.fancyBiomeColors;
		ConfigReader.write();
		((FancyGraphicsButton)getScreen().getWidget(fancyGraphics)).custom = true;
		
		if (Minecraft.theMinecraft.theWorld != null) {
			Minecraft.theMinecraft.renderGlobal.updateAllRenderers();
		}
	}
}
