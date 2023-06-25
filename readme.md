<img align="right" src="https://media.discordapp.net/attachments/945691411435622453/1119691277215211590/-1.png" height="140" width="140">

# AdventurePAPI

Adventure Placeholders API - plugin that allows you to 
create your own placehoders for strings and AdventureAPI 
components

## Placeholders

Placeholders are special formats for text that are 
replaced by specific values. This plugin allows you 
to create your own placeholders. The format of placeholders 
in the text look like this:

`{placeholder_id}`

You can also use different arguments in placeholders as follows:

`{placeholder_id:arg1|arg2|arg3|...|argN}`

## Placeholder types
AdventurePAPI allows you to make three types of placeholders:

### Targeted placeholders
Placeholders which value depends on the target 
that is passed when the placeholders are applying

Example (placeholder, which will be replaced by the name of the target):
```Java
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//Placeholder expample {name}
public class SenderNamePlaceholder extends TargetedPlaceholder<CommandSender> {
	
	/**
	 * placeholderId - placeholder id, lol \(._.)/
	 * parent - plugin to which the placeholder will be
	 *          attached. When you disable parent plugin
	 *          placeholder will be unloaded. Might be null
	 * */
	public SenderNamePlaceholder(@NotNull String placeholderId, @Nullable Plugin parent) {
		super(placeholderId, parent);
	}

	/**
	 * This method returns the value to be placed in the 
	 * string or component instead of this placeholder.
	 *
	 * target - target of this placeholder
	 * args - arguments from placeholder format {placeholderId:arg1|arg2|arg3|...}
	 *
	 * If this method returns null, the placeholder remains 
	 * in the string or component without any changes,
	 * otherwise, placeholder will be replaced
	 * by returned value
	 * */
	@NotNull
	@Override
	public Object apply(@NotNull CommandSender target, @NotNull String[] args) {
	    return target.getName();
	}
	
}
```

### BiTargeted placeholders

Placeholders which value depends on the two targets
which are passed when the placeholders are applying

Example (placeholder, which will be replaced by the first target's name if the second target can see it):

```Java
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.xikki.plugins.adventurepapi.placeholders.BiTargetedPlaceholder;

public class VisiblePlayerNamePlaceholder extends BiTargetedPlaceholder<Player, Player> {

	/**
	 * placeholderId - placeholder id, lol \(._.)/
	 * parent - plugin to which the placeholder will be
	 *          attached. When you disable parent plugin
	 *          placeholder will be unloaded. Might be null
	 * */
	public VisiblePlayerNamePlaceholder(@NotNull String placeholderId, @Nullable Plugin parent) {
		super(placeholderId, parent);
	}

	/**
	 * This method returns the value to be placed in the 
	 * string or component instead of this placeholder.
	 *
	 * firstTarget - first target of this placeholder
	 * secondTarget - second target of this placeholder
	 * args - arguments from placeholder format {placeholderId:arg1|arg2|arg3|...}
	 *
	 * If this method returns null, the placeholder remains 
	 * in the string or component without any changes,
	 * otherwise, placeholder will be replaced
	 * by returned value
	 * */
	@NotNull
	@Override
	public Object apply(@NotNull Player firstTarget, @NotNull Player secondTarget, @NotNull String[] args) {
		Component component = Component.text(firstTarget.getName());
		if (!secondTarget.canSee(firstTarget))
			component = component.decoration(TextDecoration.OBFUSCATED, true);
		return component;
	}

}
```

### NonTargeted placeholders
Placeholders which value don't depends on the target

Example (placeholder that will set the color of the component after it from the HEX string)
```Java
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import ru.xikki.plugins.adventurepapi.placeholders.NonTargetedPlaceholder;
import java.awt.Color;

//Placeholder example {color:#394eb8}
public class ColorPlaceholder extends NonTargetedPlaceholder {

	/**
	 * placeholderId - placeholder id, lol \(._.)/
	 * parent - plugin to which the placeholder will be
	 *          attached. When you disable parent plugin
	 *          placeholder will be unloaded. Might be null
	 * */
	public ColorPlaceholder(@NotNull String placeholderId, @Nullable Plugin parent) {
		super(placeholderId, parent);
	}
	
	/**
	 * This method returns the value to be placed in the 
	 * string or component instead of this placeholder.
	 *
	 * args - arguments from placeholder format {placeholderId:arg1|arg2|arg3|...}
	 *
	 * If this method returns null, the placeholder remains 
	 * in the string or component without any changes,
	 * otherwise, placeholder will be replaced
	 * by returned value
	 * */
	@NotNull
	@Override
	public Object apply(@NotNull String[] args) {
		if (args.length != 1) //Color is not set in placeholder
			return null;
		String hex = args[0];
		if (!hex.matches("#[0-9A-Fa-f]{6}")) //Is not a HEX string
			return null;
		Color color = Color.decode(hex);
		return Component.text().color(TextColor.color(color.getRGB()));
	}
	
}
```

### Placeholders applying

Without targets:
```Java
String rawStr = ...;
Component rawComponent = ...;

String formattedStr = AdventurePAPI.applyPlaceholders(rawStr);
Component formattedComponent = AdventurePAPI.applyPlaceholders(rawComponent);
```

With one target:
```Java
Object target = ...;

String rawStr = ...;
Component rawComponent = ...;

String formattedStr = AdventurePAPI.applyPlaceholders(rawStr, target);
Component formattedComponent = AdventurePAPI.applyPlaceholders(rawComponent, target);
```

With two targets:
```Java
Object firstTarget = ...;
Object secondTarget = ...;

String rawStr = ...;
Component rawComponent = ...;

String formattedStr = AdventurePAPI.applyPlaceholders(rawStr, firstTarget, secondTarget);
Component formattedComponent = AdventurePAPI.applyPlaceholders(rawComponent, firstTarget, secondTarget);
```


### Placeholder registration
```Java
JavaPlugin parent = ...;
ColorPlaceholder placeholder = new ColorPlaceholder("color", parent);
placeholder.register();
```

### Getting placeholder by Id
```Java
ColorPlaceholder placeholder = (ColorPlaceholder) AbstractPlaceholder.from("color");
```

### Placeholder unregistering
```Java
ColorPlaceholder placeholder = (ColorPlaceholder) AbstractPlaceholder.from("color");
if (placeholder != null)
    placeholder.unregister();
```

### Placeholder settings
Also, placeholders have settings that allow you to 
adjust how formats are applied when you apply placeholders.

`{placeholderId:arg1|arg2|arg3|...}` - default placeholder, 
which will be applied without any changes


`{placeholderId:arg1|arg2|arg3|...}+` - extended placeholder,
which format will be extended to the following components

### Examples

#### &8This text colored blue. {name} again blue
<img src="https://media.discordapp.net/attachments/945691411435622453/1119718071729389718/image.png">

<br>

#### &8This text colored blue. {name}+ and this text white. &8Again blue
<img src="https://media.discordapp.net/attachments/945691411435622453/1119718433689436410/image.png">
