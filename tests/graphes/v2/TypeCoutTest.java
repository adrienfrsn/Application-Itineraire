package tests.graphes.v2;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.ulille.but.sae_s2_2024.*;
import src.v1.CheminImpl;
import src.v1.LieuImpl;
import src.v1.TranconImpl;
import src.v1.TypeCout;

public class TypeCoutTest {

    @Test
    void testGetUnit() {

        assertEquals("€", TypeCout.getUnit(TypeCout.PRIX));
        assertEquals("min", TypeCout.getUnit(TypeCout.TEMPS));
        assertEquals("kgCO2", TypeCout.getUnit(TypeCout.CO2));

    }

}