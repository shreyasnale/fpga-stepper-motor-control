library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.NUMERIC_STD.ALL;

entity patternhs is
    Port ( clk : in STD_LOGIC;
           resetn : in STD_LOGIC;
           direction : inout STD_LOGIC;
           done : out STD_LOGIC;
           stptime : in STD_LOGIC_VECTOR (23 downto 0);
           posref : in STD_LOGIC_VECTOR (15 downto 0);
           pos : out STD_LOGIC_VECTOR (15 downto 0);
           a1p : out STD_LOGIC; a1n : out STD_LOGIC;
           a2p : out STD_LOGIC; a2n : out STD_LOGIC;
           b1p : out STD_LOGIC; b1n : out STD_LOGIC;
           b2p : out STD_LOGIC; b2n : out STD_LOGIC );
end patternhs;

architecture Behavioral of patternhs is
    type step_sequence is (idle, STEP_1, STEP_2, STEP_3, STEP_4, STEP_5, STEP_6, STEP_7, STEP_8);
    signal current_step : step_sequence := idle;
    signal next_step : step_sequence := step_1;
    SUBTYPE int16_type IS integer range -32758 to 32757;
    signal pos_int : int16_type;
    signal posref_int : int16_type;
    signal start : std_logic := '0';
    signal prev_direction : std_logic := '0';
    signal step_counter : integer := 0;
    signal stptime_int : integer;
begin
    pos <= std_logic_vector(to_signed(pos_int, pos'length));
    posref_int <= to_integer(signed(posref));
    stptime_int <= to_integer(unsigned(stptime));

    process (clk)
    begin
        if rising_edge(clk) then
            if resetn = '0' then
                current_step <= idle;
                pos_int <= 0;
                direction <= '0';
                done <= '0';
                step_counter <= 0;
            else
                if step_counter < stptime_int then
                    step_counter <= step_counter + 1;
                else
                    step_counter <= 0;
                    if posref_int < pos_int then
                        pos_int <= pos_int - 1;
                        direction <= '1';
                        start <= '1';
                        current_step <= next_step;
                    elsif posref_int > pos_int then
                        pos_int <= pos_int + 1;
                        direction <= '0';
                        start <= '1';
                        current_step <= next_step;
                    else
                        start <= '0';
                        current_step <= current_step;
                        done <= '1';
                    end if;
                    prev_direction <= direction;
                end if;
            end if;
        end if;
    end process;

    process (current_step, direction, start, posref_int, pos_int)
    begin
        b2p <= '0'; b1n <= '0'; b1p <= '0'; b2n <= '0';
        a1p <= '0'; a2n <= '0'; a2p <= '0'; a1n <= '0';
        next_step <= STEP_1;
        case current_step is
            when idle =>
                if start = '1' then
                    if posref_int > pos_int then next_step <= STEP_1; else next_step <= STEP_8; end if;
                end if;
            when STEP_1 => b2p <= '1'; b1n <= '1';
                if posref_int > pos_int then next_step <= STEP_2; elsif posref_int = pos_int then if direction='0' then next_step<=STEP_8; else next_step<=STEP_2; end if; else next_step<=STEP_8; end if;
            when STEP_2 => b2p <= '1'; b1n <= '1'; a1p <= '1'; a2n <= '1';
                if posref_int > pos_int then next_step<=STEP_3; elsif posref_int=pos_int then if direction='0' then next_step<=STEP_1; else next_step<=STEP_3; end if; else next_step<=STEP_1; end if;
            when STEP_3 => a1p <= '1'; a2n <= '1';
                if posref_int > pos_int then next_step<=STEP_4; elsif posref_int=pos_int then if direction='0' then next_step<=STEP_2; else next_step<=STEP_4; end if; else next_step<=STEP_2; end if;
            when STEP_4 => a1p <= '1'; a2n <= '1'; b1p <= '1'; b2n <= '1';
                if posref_int > pos_int then next_step<=STEP_5; elsif posref_int=pos_int then if direction='0' then next_step<=STEP_3; else next_step<=STEP_5; end if; else next_step<=STEP_3; end if;
            when STEP_5 => b1p <= '1'; b2n <= '1';
                if posref_int > pos_int then next_step<=STEP_6; elsif posref_int=pos_int then if direction='0' then next_step<=STEP_4; else next_step<=STEP_6; end if; else next_step<=STEP_4; end if;
            when STEP_6 => b1p <= '1'; b2n <= '1'; a2p <= '1'; a1n <= '1';
                if posref_int > pos_int then next_step<=STEP_7; elsif posref_int=pos_int then if direction='0' then next_step<=STEP_5; else next_step<=STEP_7; end if; else next_step<=STEP_5; end if;
            when STEP_7 => a2p <= '1'; a1n <= '1';
                if posref_int > pos_int then next_step<=STEP_8; elsif posref_int=pos_int then if direction='0' then next_step<=STEP_6; else next_step<=STEP_8; end if; else next_step<=STEP_6; end if;
            when STEP_8 => a2p <= '1'; a1n <= '1'; b2p <= '1'; b1n <= '1';
                if posref_int > pos_int then next_step<=STEP_1; elsif posref_int=pos_int then if direction='0' then next_step<=STEP_7; else next_step<=STEP_1; end if; else next_step<=STEP_7; end if;
        end case;
    end process;
end Behavioral;
