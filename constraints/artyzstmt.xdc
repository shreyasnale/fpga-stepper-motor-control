##Switches
set_property -dict { PACKAGE_PIN M20  IOSTANDARD LVCMOS33 } [get_ports { SWITCH_art[0] }]; #IO_L7N_T1_AD2N_35 Sch=SW0
set_property -dict { PACKAGE_PIN M19  IOSTANDARD LVCMOS33 } [get_ports { SWITCH_art[1] }]; #IO_L7P_T1_AD2P_35 Sch=SW1

##Buttons
set_property -dict { PACKAGE_PIN D19    IOSTANDARD LVCMOS33 } [get_ports { PUSHB_art[0] }]; #IO_L4P_T0_35 Sch=BTN0
set_property -dict { PACKAGE_PIN D20    IOSTANDARD LVCMOS33 } [get_ports { PUSHB_art[1] }]; #IO_L4N_T0_35 Sch=BTN1
set_property -dict { PACKAGE_PIN L20    IOSTANDARD LVCMOS33 } [get_ports { PUSHB_art[2] }]; #IO_L9N_T1_DQS_AD3N_35 Sch=BTN2
set_property -dict { PACKAGE_PIN L19    IOSTANDARD LVCMOS33 } [get_ports { PUSHB_art[3] }]; #IO_L9P_T1_DQS_AD3P_35 Sch=BTN3

##RGB LEDs
set_property -dict { PACKAGE_PIN L15    IOSTANDARD LVCMOS33 } [get_ports { LED_blu[0] }]; #IO_L22N_T3_AD7P_35 Sch=LED4_B
set_property -dict { PACKAGE_PIN G14    IOSTANDARD LVCMOS33 } [get_ports { LED_blu[1] }]; #IO_0_35 Sch=LED5_B
set_property -dict { PACKAGE_PIN G17    IOSTANDARD LVCMOS33 } [get_ports { LED_grn[0] }]; #IO_L16P_T2_35 Sch=LED4_G
set_property -dict { PACKAGE_PIN L14    IOSTANDARD LVCMOS33 } [get_ports { LED_grn[1] }]; #IO_L22P_T3_AD7P_35 Sch=LED5_G
set_property -dict { PACKAGE_PIN N15    IOSTANDARD LVCMOS33 } [get_ports { LED_red[0] }]; #IO_L21P_T3_DQS_AD14P_35 Sch=LED4_R
set_property -dict { PACKAGE_PIN M15    IOSTANDARD LVCMOS33 } [get_ports { LED_red[1] }]; #IO_L23N_T3_35 Sch=LED5_R

##LEDs
set_property -dict { PACKAGE_PIN R14    IOSTANDARD LVCMOS33 } [get_ports { LED_hig[0] }]; #IO_L6N_T0_VREF_34 Sch=LED0
set_property -dict { PACKAGE_PIN P14    IOSTANDARD LVCMOS33 } [get_ports { LED_hig[1] }]; #IO_L6P_T0_34 Sch=LED1
set_property -dict { PACKAGE_PIN N16    IOSTANDARD LVCMOS33 } [get_ports { LED_hig[2] }]; #IO_L21N_T3_DQS_AD14N_35 Sch=LED2
set_property -dict { PACKAGE_PIN M14    IOSTANDARD LVCMOS33 } [get_ports { LED_hig[3] }]; #IO_L23P_T3_35 Sch=LED3

## Pmod Header JB
set_property -dict { PACKAGE_PIN W14   IOSTANDARD LVCMOS33 } [get_ports { a1p }]; #IO_L8P_T1_34 Sch=JB1_P
set_property -dict { PACKAGE_PIN Y14   IOSTANDARD LVCMOS33 } [get_ports { a1n }]; #IO_L8N_T1_34 Sch=JB1_N
set_property -dict { PACKAGE_PIN T11   IOSTANDARD LVCMOS33 } [get_ports { a2p }]; #IO_L1P_T0_34 Sch=JB2_P
set_property -dict { PACKAGE_PIN T10   IOSTANDARD LVCMOS33 } [get_ports { a2n }]; #IO_L1N_T0_34 Sch=JB2_N
set_property -dict { PACKAGE_PIN V16   IOSTANDARD LVCMOS33 } [get_ports { b1p }]; #IO_L18P_T2_34 Sch=JB3_P
set_property -dict { PACKAGE_PIN W16   IOSTANDARD LVCMOS33 } [get_ports { b1n }]; #IO_L18N_T2_34 Sch=JB3_N
set_property -dict { PACKAGE_PIN V12   IOSTANDARD LVCMOS33 } [get_ports { b2p }]; #IO_L4P_T0_34 Sch=JB4_P
set_property -dict { PACKAGE_PIN W13   IOSTANDARD LVCMOS33 } [get_ports { b2n }]; #IO_L4N_T0_34 Sch=JB4_N