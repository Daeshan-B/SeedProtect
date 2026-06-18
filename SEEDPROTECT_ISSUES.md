# SeedProtect - Deprecations, Errors, and Bugs Report

## 🐛 Bugs

### 1. **Race Condition in `autoReplant()` - Line 226**
**File:** `SeedProtectorEvents.java:226`  
**Severity:** Critical  
**Description:** When auto-replanting crops, the code checks if `plantedFromHand` is false before consuming a seed from drops. However, if the player held the seed (plantedFromHand=true), the drops still contain that seed which gets dropped anyway, resulting in duplicate seeds being dropped.  
**Impact:** Players gain extra seeds when using the seed as the replant source.
**Fix:** The condition should be `if (!plantedFromHand)`, but the logic also needs to handle the case where the seed was consumed from hand - it shouldn't be in the drops to begin with.

### 2. **Missing Null Check for Block Data - Line 99**
**File:** `SeedProtectorEvents.java:99`  
**Severity:** Medium  
**Description:** The code casts `block.getBlockData()` to `Ageable` without first checking if the block data actually has the `Ageable` class. If a plugin modifies block data or if there's a compatibility issue, this will throw a `ClassCastException`.
**Impact:** Plugin crash when breaking non-crop blocks with custom data or during server updates.
**Fix:** Add `if (block.getBlockData() instanceof Ageable)` before casting.

### 3. **NPE Risk in `onCropBreak()` - Line 95**
**File:** `SeedProtectorEvents.java:95`  
**Severity:** Medium  
**Description:** The condition `!isCrop(block)` calls a method that returns a boolean, but if `block` becomes null between the event handler and the method call (unlikely but possible in edge cases), an NPE occurs.
**Fix:** Already protected by `player.isSneaking()` check, but could be defensive.

### 4. **Incorrect Particle Spawn Location - Multiple locations**
**File:** `SeedProtectorEvents.java:167, 288`  
**Severity:** Low  
**Description:** Particles are spawned at `center` location which is `.add(0.5, 0.5, 0.5)` from the block. However, `Particle.HAPPY_VILLAGER` appears to spawn slightly above the given location, making it look like it spawns too high or in the wrong position visually.
**Fix:** Either adjust the Y coordinate or use `Particle.STAR` or another particle that spawns at the exact location.

### 5. **`ThreadLocalRandom.current()` Called Twice - Line 65**
**File:** `SeedProtectorEvents.java:65`  
**Severity:** Low  
**Description:** `ThreadLocalRandom.current()` is called once in the field declaration and again in the class body. This is redundant since the field declaration already instantiates it.
**Fix:** Remove the second call on line 65.

### 6. **Unused Field `random` Declaration**
**File:** `SeedProtectorEvents.java:65`  
**Severity:** Low  
**Description:** The `ThreadLocalRandom random = ThreadLocalRandom.current();` on line 65 declares a field but the field is never used - `random.nextDouble()` is called directly instead of `this.random.nextDouble()`.
**Fix:** Remove line 65 or change all references to use `this.random`.

## ⚠️ Errors

### 7. **Hardcoded Version String - Line 22**
**File:** `SeedProtect.java:22`  
**Severity:** Low  
**Description:** The version string is hardcoded as a final static field. While this is a common pattern, it should be read from the plugin descriptor or properties file for proper version management.
**Impact:** Makes automated version parsing and changelog generation harder.

### 8. **Duplicate Message Sent to Sender - Line 30, 35, 45**
**File:** `ToggleCommand.java:30, 35, 45`  
**Severity:** Low  
**Description:** Messages are sent to both `player` and `sender`. In most cases, `sender` IS the `player`, so this results in duplicate messages. The only exception is console commands, where sender is the console.
**Impact:** Players see messages twice when executing the toggle command.
**Fix:** Remove one of the send calls, or only send to `player` since console messages are rare for this command.

### 9. **Missing Console Sender Check - Line 21**
**File:** `ToggleCommand.java:21`  
**Severity:** Medium  
**Description:** The code checks if `sender instanceof Player`, but then immediately calls `sender.sendMessage()` without null checking. While Bukkit guarantees this won't be null, it's a potential NPE if the plugin is run in a non-standard environment.
**Fix:** Add null check or rely on Bukkit's contract.

### 10. **Inconsistent Color Usage - Line 22, 29, 34**
**File:** `ToggleCommand.java:22, 29, 34`  
**Severity:** Low  
**Description:** Error messages use hardcoded `TextColor.color(255, 0, 0)` (red) instead of using a static final constant like the plugin header does with `DIM`, `GREEN`, `WHITE`.
**Impact:** Inconsistent code style, harder to maintain color consistency.

## 🔄 Deprecations

### 11. **Deprecated `ThreadLocalRandom.current()` Pattern - Line 65**
**File:** `SeedProtectorEvents.java:65`  
**Severity:** Informational  
**Description:** The pattern of calling `ThreadLocalRandom.current()` in a field declaration is not deprecated per se, but it's redundant. The modern pattern is to just use `ThreadLocalRandom.current()` inline or use `Random` for simplicity.
**Impact:** Minor code cleanliness issue.

### 12. **Bukkit API Usage - Throughout**
**File:** All files  
**Severity:** Informational  
**Description:** The plugin uses various Bukkit APIs that may have deprecated methods in future versions. Specifically:
- `org.bukkit.event.block.BlockGrowEvent` - This event is deprecated in favor of listening to `BlockGrowEvent` from the world or using block data change events
- `player.getInventory().getItemInMainHand()` - May be replaced by more direct inventory access in future versions
- `Block.setType()` - Should use `BlockData` manipulation instead
**Impact:** Plugin may break or require updates in future Minecraft versions.

### 13. **`Component.text()` with Color - All files**
**File:** All files  
**Severity:** Informational  
**Description:** The code uses `Component.text(message).color(color)` which is fine, but newer Adventure API versions might have more efficient batch methods for multiple components.
**Impact:** Performance optimization opportunity.

## 📋 Summary Table

| ID | Severity | Type | File | Description |
|----|----------|------|------|-------------|
| 1 | Critical | Bug | SeedProtectorEvents.java:226 | Duplicate seed dropping logic |
| 2 | Medium | Bug | SeedProtectorEvents.java:99 | Missing null check for block data |
| 3 | Medium | Bug | SeedProtectorEvents.java:95 | Potential null pointer exception |
| 4 | Low | Bug | SeedProtectorEvents.java:167,288 | Particle spawn location issue |
| 5 | Low | Bug | SeedProtectorEvents.java:65 | Redundant ThreadLocalRandom call |
| 6 | Low | Bug | SeedProtectorEvents.java:65 | Unused field declaration |
| 7 | Low | Error | SeedProtect.java:22 | Hardcoded version string |
| 8 | Low | Error | ToggleCommand.java:30,35,45 | Duplicate message sending |
| 9 | Medium | Error | ToggleCommand.java:21 | Missing null check |
| 10 | Low | Error | ToggleCommand.java:22,29,34 | Inconsistent color usage |
| 11 | Info | Deprecation | SeedProtectorEvents.java:65 | ThreadLocalRandom pattern |
| 12 | Info | Deprecation | All files | Bukkit API deprecation |
| 13 | Info | Deprecation | All files | Adventure API optimization |

## 🛠️ Recommended Priority Fixes

1. **Critical:** Fix the seed dropping logic in `autoReplant()` - this affects gameplay balance
2. **High:** Add null checks for block data casting - prevents crashes
3. **Medium:** Remove duplicate message sends - improves user experience
4. **Low:** Clean up unused `ThreadLocalRandom` declaration - code quality
