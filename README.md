# FPGA Stepper Motor Control

FPGA-based stepper motor control and monitoring system using a custom VHDL IP, AXI4-Lite, Zynq processing, bare-metal C firmware, UART/serial communication, and a Java desktop control interface.

## Features

- Custom VHDL stepper-motor control IP
- AXI4-Lite memory-mapped interface
- Zynq bare-metal C firmware
- Timer-interrupt-based stepping
- Eight transistor control outputs for a two-phase inverter
- FPGA I/O constraints for switches, push-buttons, LEDs and Pmod
- Java Swing control/monitoring GUI
- Serial communication using jSerialComm
- Plotting and recorded-data download
- Half-bridge error monitoring and clearing

## System Architecture

```text
             PC / Java GUI
                  |
                  | UART / Serial
                  v
        +-----------------------+
        | Zynq Processing System|
        |    + C Firmware       |
        +-----------+-----------+
                    |
                    | AXI4-Lite
                    v
        +-----------------------+
        |     Custom VHDL IP    |
        |                       |
        |  AXI Registers        |
        |  Step Sequencing      |
        |  Timer / Control      |
        |  Motor Outputs        |
        +-----------+-----------+
                    |
                    | Pmod JB
                    v
        +-----------------------+
        |   External Inverter   |
        +-----------+-----------+
                    |
                    v
              Stepper Motor
```

## Repository Structure

```text
fpga-stepper-motor-control/
├── rtl/
│   ├── stmipng_v1_0.vhd
│   └── stmipng_v1_0_S00_AXI.vhd
├── firmware/
│   └── stcmain.c
├── gui/
│   ├── FpgaCtrlF.java
│   └── SerialNetw.java
└── constraints/
    └── artyzstmt.xdc
```

## Hardware / FPGA

The RTL exposes eight motor-control outputs:

`a1p`, `a1n`, `a2p`, `a2n`, `b1p`, `b1n`, `b2p`, `b2n`.

The XDC file maps these outputs to the board Pmod JB connector and also defines the switch, push-button and LED pins.

## Firmware

The bare-metal C application:

- accesses the custom IP through its AXI base address
- implements a 9-state transistor switching sequence
- uses the Zynq private timer and interrupt controller
- supports manual phase selection and stepping
- supports start / stop control
- supports individual transistor toggling
- checks and clears the half-bridge error flag
- provides a serial command interface

### Transistor Sequence

```text
0: all off
1: Ao / B-
2: A+ / B-
3: A+ / Bo
4: A+ / B+
5: Ao / B+
6: A- / B+
7: A- / Bo
8: A- / B-
```

### Example Commands

```text
isr
start
stop
clr
p0 ... p8
+
-
a1p
a1n
a2p
a2n
b1p
b1n
b2p
b2n
t<hex>
x
```

## Java GUI

The desktop application provides:

- serial-device discovery
- connection and disconnection
- command console
- chart plotting
- recorded-data download
- periodic processing of received FPGA data

The serial connection is configured for **115200 baud, 8 data bits, 1 stop bit, no parity**.

## Tools & Technologies

| Area | Technology |
|---|---|
| HDL | VHDL |
| FPGA Design | AMD/Xilinx Vivado |
| Embedded Software | Vitis / C |
| Processing | Zynq-7000 |
| Bus | AXI4-Lite |
| Communication | UART / Serial |
| GUI | Java / Swing |
| Serial Library | jSerialComm |
| Board Interface | Pmod JB |

## Project Scope

This repository contains the source code and FPGA constraints used for the project. The original academic project report is **intentionally not included**.

## Safety Note

The FPGA outputs are intended to interface with an external inverter/driver stage rather than directly power the motor. Use the hardware only within the electrical ratings and protection requirements of the connected driver and motor.
