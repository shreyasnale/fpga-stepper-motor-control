# FPGA Stepper Motor Control & Monitoring System

An FPGA-based stepper-motor control and monitoring system implemented on a **Zynq-7000** platform. The project combines a custom **VHDL stepper-motor controller in programmable logic**, an **AXI4-Lite hardware/software interface**, **Vitis firmware**, UART communication, and a **Java PC GUI** for configuration and real-time monitoring.

> **Note:** The academic project report is intentionally **not included** in this repository. This README summarizes the technical content documented in the project.

## Project Overview

The main design goal is to keep time-critical motor control inside FPGA hardware while leaving high-level configuration, communication, and monitoring to the Zynq processing system.

The architecture therefore separates:

- **Programmable Logic (PL):** deterministic step generation, commutation, timing and position tracking
- **Processing System (PS):** configuration, command processing, interrupt-driven sampling and telemetry
- **AXI4-Lite:** memory-mapped interface between software and the custom motor-control IP
- **PC GUI:** motor parameter configuration, status monitoring and live position visualization

This hardware/software partitioning avoids relying on software execution timing for the actual step pulses. The project documentation describes the resulting approach as deterministic and suitable for repeatable motion control.

## System Architecture

```text
                    PC / Java GUI
                         |
                    UART / Serial
                         |
                         v
              +-----------------------+
              |   Zynq Processing     |
              |       System          |
              |   ARM Cortex-A9       |
              |   Vitis Firmware      |
              +-----------+-----------+
                          |
                       AXI4-Lite
                          |
                          v
              +-----------------------+
              | Custom Stepper Motor  |
              |       VHDL IP         |
              |                       |
              |  Control Registers    |
              |  Position Tracking    |
              |  Step Timing          |
              |  Commutation Logic    |
              |  Fault Handling       |
              +-----------+-----------+
                          |
                    8 gate signals
                          |
                          v
              +-----------------------+
              | External MOSFET /     |
              | H-Bridge Driver Stage |
              +-----------+-----------+
                          |
                          v
                    Stepper Motor
```

The project documentation specifies that the FPGA outputs control the external power stage rather than driving the motor coils directly.

## Key Features

- Custom VHDL stepper-motor control IP
- AXI4-Lite memory-mapped register interface
- Hardware-based deterministic step-pulse generation
- Direction and start/stop control
- Position/reference-position control
- FPGA-based timing with a 100 MHz clock
- 100 µs hardware time base for telemetry
- Eight transistor/MOSFET gate-control outputs
- Half-bridge fault monitoring
- Zynq ARM Cortex-A9 supervisory firmware
- Vitis bare-metal software
- UART/serial PC communication
- Java-based monitoring and control GUI
- Live reference-position vs. actual-position visualization
- Timestamped motion telemetry
- Firmware ring-buffer logging
- Hardware and software verification

## AXI4-Lite Register Interface

The custom IP exposes 32-bit memory-mapped registers to the Zynq processing system.

| Register | Index | Access | Purpose |
|---|---:|---|---|
| `REG_START` | 1 | Write | Enable/disable step generation |
| `REG_REFPOS` | 3 | Write | Target/reference position |
| `REG_POS` | 4 | Read | Current motor position |
| `REG_STEPCLKS` | 5 | Write | Step period in FPGA clock cycles |
| `REG_TIME100US` | 6 | Read | Hardware time counter |

The firmware accesses the registers through a volatile memory-mapped register macro. The documented design uses the AXI interface as the boundary between the supervisory software and the deterministic hardware motion engine.

## Speed Control

Motor speed is configured in software, but the actual periodic step generation is performed in hardware.

The firmware converts the requested motor speed into a step period measured in FPGA clock cycles and writes that value to `REG_STEPCLKS`. The hardware then generates evenly spaced step pulses using the **100 MHz FPGA clock**.

This approach separates:

```text
Requested RPM
     |
     v
Vitis firmware
     |
     | calculate step period
     v
REG_STEPCLKS
     |
     v
VHDL hardware timer
     |
     v
Deterministic step pulses
```

The project documentation reports a representative 250,000-clock step period at 100 MHz, corresponding to a 2.5 ms step period.

## Position Control

The controller supports a reference position and an actual hardware position counter.

The documented motion sequence is:

1. Disable motion.
2. Update `REG_REFPOS`.
3. Enable motion.
4. Hardware moves the position toward the target.
5. Motion terminates when the actual position reaches the reference position.

A stop command can interrupt motion before the target is reached, after which motion can be resumed toward the reference position.

## Telemetry & Ring Buffer

The firmware periodically samples hardware status during a timer interrupt and stores telemetry information for the PC interface.

The project documentation describes:

- 10 Hz interrupt-based sampling
- 100 ms sampling interval
- Hardware timestamps from `REG_TIME100US`
- 128-sample firmware ring buffer
- 12.8 seconds of history at 10 Hz
- Timestamp, reference-position and actual-position records
- Automatic removal of the oldest samples when the buffer becomes full

Example telemetry format:

```text
S <timestamp> <reference_position> <actual_position>
```

## Java GUI

The PC-side application provides an interface for operating and monitoring the motor controller.

### Control

- Serial-port selection and connection
- RPM configuration
- Reference-position configuration
- Start/stop control
- Direction through the reference-position sign
- Automatic start logic when required parameters are configured
- Safety interlock preventing operation before required parameters are available

### Monitoring

- Connection status
- Actual motor position
- Reference position
- Time information
- Motion status
- Communication/error feedback
- Live reference-vs-actual position visualization

The GUI uses serial communication with the Zynq firmware and provides real-time monitoring of the motor-control system.

## Hardware Outputs

The controller provides eight gate-control signals for the external power stage:

```text
A1P   A1N
A2P   A2N
B1P   B1N
B2P   B2N
```

These signals are intended for the external MOSFET/H-bridge driver stage. They are not intended to directly drive the motor windings.

## Verification & Results

The project documentation describes verification of several important functions:

- Correct step generation and speed conversion
- Position movement toward a commanded reference
- Motion termination at the target position
- Stop and subsequent resume behavior
- Regular interrupt timing
- Stable timestamp progression
- Ring-buffer operation beyond its capacity
- Correct GUI telemetry retrieval
- Consistent operation during extended testing

The report describes the interrupt timestamps as approximately constant and reports no noticeable timing instability during extended operation.

## Technologies

| Category | Technology |
|---|---|
| Hardware Description | VHDL |
| FPGA Platform | Zynq-7000 |
| FPGA Toolchain | AMD/Xilinx Vivado |
| Embedded Software | Vitis / C |
| CPU | ARM Cortex-A9 |
| Hardware/Software Bus | AXI4-Lite |
| PC Interface | UART / Serial |
| GUI | Java / Eclipse |
| Motor Driver Interface | MOSFET / H-Bridge gate signals |

## Suggested Repository Structure

```text
fpga-stepper-motor-control/
├── README.md
├── rtl/
│   ├── stepmot_v1_0.vhd
│   ├── stepmot_v1_0_S00_AXI.vhd
│   └── patternhs.vhd
├── firmware/
│   └── stcmain.c
├── gui/
│   ├── FpgaCtrlF.java
│   └── SerialNetw.java
├── constraints/
│   └── artyzstmc.xdc
└── ip_repo/
    └── stepmot_1_0/
```

Generated Vivado/Vitis build products, caches and binaries should remain excluded unless they are specifically required to reproduce the hardware build.

## Project Scope

This repository is intended to present the **technical implementation and source code** of the FPGA stepper-motor project in a clean, portfolio-friendly format. The original academic report is deliberately excluded.

## Safety

The FPGA gate outputs interface with an external power stage. Hardware must be operated only with the appropriate driver circuitry, electrical isolation/protection, current limits and motor ratings.