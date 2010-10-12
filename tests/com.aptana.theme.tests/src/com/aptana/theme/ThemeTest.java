/**
 * This file Copyright (c) 2005-2010 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain other free and open source software ("FOSS") code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.theme;

import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

@SuppressWarnings("nls")
public class ThemeTest extends TestCase
{

	private Theme theme;
	private ColorManager colormanager;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		Properties props = new Properties();
		props.put("background", "#ffffff");
		props.put("foreground", "#ff0000");
		props.put("caret", "#00ff00");
		props.put("selection", "#0000ff");
		props.put("lineHighlight", "#ff00ff");
		props.put("name", "chris");
		props.put("constant", "#00ff00,#ff00ff,italic");
		props.put("constant.language.js", "#000000,#ff0000,bold");
		colormanager = new ColorManager();
		theme = new Theme(colormanager, props)
		{
			@Override
			protected void storeDefaults()
			{
				// do nothing
			}

			@Override
			public void save()
			{
				// do nothing
			}

			protected void addTheme(Theme newTheme)
			{
				// do nothing
			};
		};
	}

	public void testBasics()
	{
		assertEquals("chris", theme.getName());
		assertBasics(theme);
	}

	private void assertBasics(Theme theme)
	{
		assertEquals(new RGB(255, 255, 255), theme.getBackground());
		assertEquals(new RGB(255, 0, 0), theme.getForeground());
		assertEquals(new RGB(0, 255, 0), theme.getCaret());
		assertEquals(new RGBa(0, 0, 255, 255), theme.getSelection());
		assertEquals(new RGBa(255, 0, 255, 255), theme.getLineHighlight());
		// Now check tokens
		assertTrue(theme.hasEntry("constant.language.js"));
		assertFalse(theme.hasEntry("constant.language"));
		assertTrue(theme.hasEntry("constant"));

		// Check "constant" token colors
		assertEquals(new RGB(0, 255, 0), theme.getTextAttribute("constant").getForeground().getRGB());
		assertEquals(new RGB(0, 255, 0), theme.getForegroundAsRGB("constant"));
		assertEquals(new RGB(255, 0, 255), theme.getTextAttribute("constant").getBackground().getRGB());
		assertEquals(new RGB(255, 0, 255), theme.getBackgroundAsRGB("constant"));
		// "constant.langauge inherits "constant" token's colors
		assertEquals(theme.getTextAttribute("constant"), theme.getTextAttribute("constant.language"));
		assertEquals(new RGB(0, 255, 0), theme.getForegroundAsRGB("constant.language"));
		assertEquals(new RGB(255, 0, 255), theme.getBackgroundAsRGB("constant.language"));
		// "constant.language.js" has overridden parents, so it has it's own colors
		assertEquals(new RGB(0, 0, 0), theme.getTextAttribute("constant.language.js").getForeground().getRGB());
		assertEquals(new RGB(0, 0, 0), theme.getForegroundAsRGB("constant.language.js"));
		assertEquals(new RGB(255, 0, 0), theme.getTextAttribute("constant.language.js").getBackground().getRGB());
		assertEquals(new RGB(255, 0, 0), theme.getBackgroundAsRGB("constant.language.js"));
	}

	public void testAddingTokens()
	{
		assertFalse(theme.hasEntry("chris"));
		theme.addNewDefaultToken("chris");
		assertTrue(theme.hasEntry("chris"));
		assertEquals(new RGB(255, 0, 0), theme.getForegroundAsRGB("chris"));
	}

	public void testModifyingTokens()
	{
		TextAttribute at = new TextAttribute(colormanager.getColor(new RGB(128, 128, 128)), colormanager
				.getColor(new RGB(64, 0, 64)), TextAttribute.UNDERLINE);
		theme.update("constant", at);
		assertEquals(new RGB(128, 128, 128), theme.getForegroundAsRGB("constant.language"));
		assertEquals(new RGB(64, 0, 64), theme.getBackgroundAsRGB("constant.language"));
	}

	public void testUpdateGlobalBGColor()
	{
		theme.updateBG(new RGB(128, 128, 128));
		assertEquals(new RGB(128, 128, 128), theme.getBackground());
		theme.updateBG(null);
		assertEquals(new RGB(128, 128, 128), theme.getBackground());
	}

	public void testGetBackAsRGBReturnsThemeBackgroundIfNoBackGroundSpecified()
	{
		assertEquals(theme.getBackground(), theme.getBackgroundAsRGB("something.that.inherits"));
	}

	public void testUpdateGlobalFGColor()
	{
		theme.updateFG(new RGB(128, 128, 128));
		assertEquals(new RGB(128, 128, 128), theme.getForeground());
		theme.updateFG(null);
		assertEquals(new RGB(128, 128, 128), theme.getForeground());
		assertEquals(new RGB(128, 128, 128), theme.getForegroundAsRGB("something.that.inherits"));
	}

	public void testUpdateGlobalCaretColor()
	{
		theme.updateCaret(new RGB(128, 128, 128));
		assertEquals(new RGB(128, 128, 128), theme.getCaret());
		theme.updateCaret(null);
		assertEquals(new RGB(128, 128, 128), theme.getCaret());
	}

	public void testUpdateGlobalLineHighlightColor()
	{
		theme.updateLineHighlight(new RGB(128, 128, 128));
		assertEquals(new RGBa(128, 128, 128, 255), theme.getLineHighlight());
		theme.updateLineHighlight(null);
		assertEquals(new RGBa(128, 128, 128, 255), theme.getLineHighlight());
	}

	public void testUpdateGlobalSelectionColor()
	{
		theme.updateSelection(new RGB(128, 128, 128));
		assertEquals(new RGBa(128, 128, 128, 255), theme.getSelection());
		theme.updateSelection(null);
		assertEquals(new RGBa(128, 128, 128, 255), theme.getSelection());
	}

	public void testGetTokens()
	{
		Map<String, TextAttribute> tokens = theme.getTokens();
		assertEquals(2, tokens.size());
		assertTrue(tokens.containsKey("constant"));
		assertTrue(tokens.containsKey("constant.language.js"));
		assertFalse(tokens.containsKey("whatever"));
		TextAttribute at = tokens.get("constant");
		assertNotNull(at);
		assertEquals(new RGB(0, 255, 0), at.getForeground().getRGB());
		assertEquals(new RGB(255, 0, 255), at.getBackground().getRGB());
		assertEquals(SWT.ITALIC, at.getStyle());

		at = tokens.get("constant.language.js");
		assertNotNull(at);
		assertEquals(new RGB(0, 0, 0), at.getForeground().getRGB());
		assertEquals(new RGB(255, 0, 0), at.getBackground().getRGB());
		assertEquals(SWT.BOLD, at.getStyle());
	}

	public void testCopy()
	{
		// TODO What if we try to copy with a name that's already taken!
		Theme copy = theme.copy("chris_copy");
		assertEquals("chris_copy", copy.getName());
		assertBasics(copy);
	}

	public void testCopyWithNullArgument()
	{
		assertNull(theme.copy(null));
	}

	// TODO Add test for delete

	public void testRemove()
	{
		// Now check tokens
		assertTrue(theme.hasEntry("constant.language.js"));
		assertTrue(theme.hasEntry("constant"));

		// Check "constant" token colors
		assertEquals(new RGB(0, 255, 0), theme.getTextAttribute("constant").getForeground().getRGB());
		assertEquals(new RGB(0, 255, 0), theme.getForegroundAsRGB("constant"));
		assertEquals(new RGB(255, 0, 255), theme.getTextAttribute("constant").getBackground().getRGB());
		assertEquals(new RGB(255, 0, 255), theme.getBackgroundAsRGB("constant"));
		// "constant.language.js" has overridden parents, so it has it's own colors
		assertEquals(new RGB(0, 0, 0), theme.getTextAttribute("constant.language.js").getForeground().getRGB());
		assertEquals(new RGB(0, 0, 0), theme.getForegroundAsRGB("constant.language.js"));
		assertEquals(new RGB(255, 0, 0), theme.getTextAttribute("constant.language.js").getBackground().getRGB());
		assertEquals(new RGB(255, 0, 0), theme.getBackgroundAsRGB("constant.language.js"));

		theme.remove("constant.language.js");

		// Now check tokens
		assertFalse(theme.hasEntry("constant.language.js"));
		assertTrue(theme.hasEntry("constant"));

		// Check "constant" token colors
		assertEquals(new RGB(0, 255, 0), theme.getTextAttribute("constant").getForeground().getRGB());
		assertEquals(new RGB(0, 255, 0), theme.getForegroundAsRGB("constant"));
		assertEquals(new RGB(255, 0, 255), theme.getTextAttribute("constant").getBackground().getRGB());
		assertEquals(new RGB(255, 0, 255), theme.getBackgroundAsRGB("constant"));
		// "constant.language.js" is removed, so it should pick up "constant"'s colors now
		assertEquals(new RGB(0, 255, 0), theme.getTextAttribute("constant.language.js").getForeground().getRGB());
		assertEquals(new RGB(0, 255, 0), theme.getForegroundAsRGB("constant.language.js"));
		assertEquals(new RGB(255, 0, 255), theme.getTextAttribute("constant.language.js").getBackground().getRGB());
		assertEquals(new RGB(255, 0, 255), theme.getBackgroundAsRGB("constant.language.js"));
	}
}
