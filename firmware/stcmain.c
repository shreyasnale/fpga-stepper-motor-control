/*
 * stcmain.c: simple test application
 *
 * This application configures UART 16550 to baud rate 9600.
 * PS7 UART (Zynq) is not initialized by this application, since
 * bootrom/bsp configures it to baud rate 115200
 *
 * ------------------------------------------------
 * | UART TYPE   BAUD RATE                        |
 * ------------------------------------------------
 *   uartns550   9600
 *   uartlite    Configurable only in HW design
 *   ps7_uart    115200 (configured by bootrom/bsp)
 */

#include <stdio.h>
#include <string.h>
#include "platform.h"
#include "xil_printf.h"
#include "xparameters.h"
#include "xscugic.h"
#include "xil_exception.h"
#include "xscutimer.h"


#define STREG(k)  *(volatile unsigned int *)(XPAR_STMIPNG_0_S00_AXI_BASEADDR+4*k)


// ---- interrupt controller -----
static XScuGic  Intc;					// interrupt controller instance
static XScuGic_Config  *IntcConfig;		// configuration instance

// ---- scu timer -----
static XScuTimer  pTimer;				// private Timer instance
static XScuTimer_Config  *pTimerConfig;	// configuration instance
#define TIMER_LOAD_VALUE  3333330		// should be 100 Hz

static volatile int  ISR_LEDs, ISR_Count;
static volatile int  Do_Steps;
static int Isr_Phase;
/*
** transistor switch sequence:
**   0: all off
**   1: Ao / B-
**   2: A+ / B-
**   3: A+ / Bo
**   4: A+ / B+
**   5: Ao / B+
**   6: A- / B+
**   7: A- / Bo
**   8: A- / B-
*/
#define SEQUENCE_LEN 9
const static unsigned int Tr_Seq[SEQUENCE_LEN] = { 0x00, 0x60, 0x69, 0x09, 0x99, 0x90, 0x96, 0x06, 0x66 };


#define CMD_LEN 64



static void SetTrans(unsigned int trans)
{
	unsigned int a1p, a1n, a2p, a2n, b1p, b1n, b2p, b2n;
	unsigned int hb_error_flag;

	STREG(1) = trans;
	a1p = 0; a1n = 0; a2p = 0; a2n = 0; b1p = 0; b1n = 0; b2p = 0; b2n = 0;
	if ((trans & 0x01) != 0) a1p = 1;
	if ((trans & 0x02) != 0) a1n = 1;
	if ((trans & 0x04) != 0) a2p = 1;
	if ((trans & 0x08) != 0) a2n = 1;
	if ((trans & 0x10) != 0) b1p = 1;
	if ((trans & 0x20) != 0) b1n = 1;
	if ((trans & 0x40) != 0) b2p = 1;
	if ((trans & 0x80) != 0) b2n = 1;
	printf("----------------------------------------------\n\r");
	printf("  A1p : %d   A2p : %d            B1p : %d  B2p: %d\n\r", a1p, a2p, b1p, b2p);
	printf("  A1n : %d   A2n : %d            B1n : %d  B2n: %d\n\r", a1n, a2n, b1n, b2n);
	printf("----------------------------------------------\n\r");
	hb_error_flag = STREG(2);
	if ((hb_error_flag & 0x01) != 0) {
		printf(" ! transistor halfbridge short error.\n\r");
	}
}



/*
 * ------------------------------------------------------------
 * Interrupt handler (ZYNQ private timer)
 * ------------------------------------------------------------
 */
static void TimerIntrHandler(void *CallBackRef)
{
	XScuTimer *TimerInstance = (XScuTimer *)CallBackRef;

	XScuTimer_ClearInterruptStatus(TimerInstance);
	if (Do_Steps == 1) {
		Isr_Phase += 1;
		if ((Isr_Phase < 1) || (Isr_Phase > SEQUENCE_LEN-1)) {
			Isr_Phase = 1;
		}
		STREG(1) = Tr_Seq[Isr_Phase];
	}
	ISR_Count++;
	STREG(0) = ISR_LEDs;
	if (ISR_LEDs == 1) {
		ISR_LEDs = 0x08;
	} else {
		ISR_LEDs >>= 1;
	}
}



int main()
{
	unsigned int xx, trans;
	char cmd_buf[CMD_LEN], *chp;
	int terminate, stlen, isr_run;
	int p_index;

    init_platform();
    print("--- stmc ivp V0.b ---\n\r");
    STREG(0) = 0x022;

    // all transistors off
    p_index = 0;
    trans = 0;
    STREG(1) = trans;

    Isr_Phase = 0;
    ISR_LEDs = 1;  ISR_Count = 0;
    print(" * initialize exceptions...\n\r");
    Xil_ExceptionInit();

    print(" * lookup config GIC...\n\r");
    IntcConfig = XScuGic_LookupConfig(XPAR_SCUGIC_0_DEVICE_ID);
    print(" * initialize GIC...\n\r");
    XScuGic_CfgInitialize(&Intc, IntcConfig, IntcConfig->CpuBaseAddress);

	// Connect the interrupt controller interrupt handler to the hardware
    print(" * connect interrupt controller handler...\n\r");
	Xil_ExceptionRegisterHandler(XIL_EXCEPTION_ID_IRQ_INT,
				(Xil_ExceptionHandler)XScuGic_InterruptHandler, &Intc);

    print(" * lookup config scu timer...\n\r");
    pTimerConfig = XScuTimer_LookupConfig(XPAR_XSCUTIMER_0_DEVICE_ID);
    print(" * initialize scu timer...\n\r");
    XScuTimer_CfgInitialize(&pTimer, pTimerConfig, pTimerConfig->BaseAddr);
    print(" * Enable Auto reload mode...\n\r");
	XScuTimer_EnableAutoReload(&pTimer);
    print(" * load scu timer...\n\r");
    XScuTimer_LoadTimer(&pTimer, TIMER_LOAD_VALUE);

    print(" * set up timer interrupt...\n\r");
    XScuGic_Connect(&Intc, XPAR_SCUTIMER_INTR, (Xil_ExceptionHandler)TimerIntrHandler,
    				(void *)&pTimer);
    print(" * enable interrupt for timer at GIC...\n\r");
    XScuGic_Enable(&Intc, XPAR_SCUTIMER_INTR);
    print(" * enable interrupt on timer...\n\r");
    XScuTimer_EnableInterrupt(&pTimer);

	// Enable interrupts in the Processor.
    print(" * enable processor interrupts...\n\r");
	Xil_ExceptionEnable();
	Do_Steps = 0;
    isr_run = 0;

    terminate = 0;
    do {
    	print(">> ");
    	fgets(cmd_buf, CMD_LEN, stdin);
    	cmd_buf[CMD_LEN-1] = '\0';
    	chp = cmd_buf;
    	while ((*chp!='\0') && (*chp!='\n') && (*chp!='\r'))  chp++;
    	*chp = '\0';
    	if (cmd_buf[0] == 'x') {
    		terminate = 1;
    	} else if (cmd_buf[0] == 'p') {
    		if (strlen(cmd_buf) < 2) {
    			printf(" *** phase number (0..8) missing.\n\r");
    		} else {
    			if (sscanf(&cmd_buf[1], "%d", &xx) != 1) {
    				printf(" *** illegal int\n\r");
    			} else {
    				if (xx > (SEQUENCE_LEN-1)) {
        				printf(" *** illegal phase number (%d)\n\r", xx);
    				} else {
    					p_index = xx;
    					trans = Tr_Seq[p_index];
    		    		SetTrans(trans);
    				}
    			}
    		}
    		printf("=> p_index: %d\n\r", p_index);
    	} else if (cmd_buf[0] == '+') {
    		p_index += 1;
    		if (p_index > (SEQUENCE_LEN-1)) {
    			p_index = 1;
    		}
			trans = Tr_Seq[p_index];
    		SetTrans(trans);
    		printf("=> p_index: %d\n\r", p_index);
    	} else if (cmd_buf[0] == '-') {
    		p_index -= 1;
    		if (p_index == 0) {
    			p_index = SEQUENCE_LEN-1;
    		}
			trans = Tr_Seq[p_index];
    		SetTrans(trans);
    		printf("=> p_index: %d\n\r", p_index);
    	} else if (!strcmp(cmd_buf, "a1p")) {
    		trans ^= 0x01;
    		SetTrans(trans);
    	} else if (!strcmp(cmd_buf, "a1n")) {
    		trans ^= 0x02;
    		SetTrans(trans);
    	} else if (!strcmp(cmd_buf, "a2p")) {
    		trans ^= 0x04;
    		SetTrans(trans);
    	} else if (!strcmp(cmd_buf, "a2n")) {
    		trans ^= 0x08;
    		SetTrans(trans);
    	} else if (!strcmp(cmd_buf, "b1p")) {
    		trans ^= 0x10;
    		SetTrans(trans);
    	} else if (!strcmp(cmd_buf, "b1n")) {
    		trans ^= 0x20;
    		SetTrans(trans);
    	} else if (!strcmp(cmd_buf, "b2p")) {
    		trans ^= 0x40;
    		SetTrans(trans);
    	} else if (!strcmp(cmd_buf, "b2n")) {
    		trans ^= 0x80;
    		SetTrans(trans);
    	} else if (!strcmp(cmd_buf, "clr")) {
    		trans = 0;
    		SetTrans(trans);
    		STREG(2) = 0;
    		SetTrans(trans);
    		printf(" => error flag cleared.\n\r");
    	} else if (cmd_buf[0] == 't') {
    		if ((stlen = strlen(cmd_buf)) < 2) {
    			printf("*** illegal cmd length (%d).\n\r", stlen);
    		} else {
    			xx = 99;
    			if (sscanf(&cmd_buf[1], "%x", &xx) != 1) {
        			printf("*** illegal value.\n\r");
    			} else if (xx > 0x0ff) {
        			printf("*** illegal transistor value (%x).\n\r", xx);
    			} else {
    				trans = xx;
    				SetTrans(trans);
    			}
    		}
    	} else if (!strcmp(cmd_buf, "isr")) {
    		if (isr_run == 0) {
    		    // start scu timer
    		    print(" * start timer...\n\r");
    		    XScuTimer_Start(&pTimer);
    		    isr_run = 1;
    		} else {
    		    // stop scu timer
    		    print(" * stop timer...\n\r");
    		    XScuTimer_Stop(&pTimer);
    			isr_run = 0;
    		}
    		xil_printf("ISR count: %d\n\r", ISR_Count);
    	} else if (!strcmp(cmd_buf, "start")) {
    		if (isr_run == 0) {
    		    printf(" *** interrupts must be on.\n\r");
    		} else {
    			Do_Steps = 1;
    		}
    	} else if (!strcmp(cmd_buf, "stop")) {
    			Do_Steps = 0;
        		trans = 0;
        		SetTrans(trans);
    	} else {
    	    xx = STREG(0);
    	    printf("sw/bt: %x\n\r", xx);
    	}
    } while (terminate == 0);

    print("shutting down...\n\r");
    XScuTimer_Stop(&pTimer);
	Xil_ExceptionDisable();
    XScuTimer_DisableInterrupt(&pTimer);
    XScuGic_Disable(&Intc, XPAR_SCUTIMER_INTR);
    // all transistors off;
    STREG(1) = 0;
    STREG(0) = 0;

    print("Thank you for using stc test.\n\r");
    cleanup_platform();
    return 0;
}