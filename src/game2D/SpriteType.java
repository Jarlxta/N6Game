package game2D;

public enum SpriteType {
    // May or may not be used
    // Player is meant to define the player character in a normal state
    // Enemy is meant to define normal enemies
    // SpectralPlayer is meant to define the player, perhaps in God Mode or with certain buffs, intended to allow bypassing collisions
    // SpectralEnemy is meant to define enemies that might be able to ignore collisions with tiles
    // Projectile is meant to define sprites that are attacks
    // add environment? maybe or maybe not, perhaps consider null value as environment.

    Player, Enemy, SpectralPlayer, SpectralEnemy, Projectile
}
