# CPU Scheduling Comparison Project
## Round Robin vs Priority Scheduling

---

## Team Members & Contributions
* **Mohamed Magdy**: System Integration, Core Process/Time Slot Model, Comparison & Conclusion Section creation and Project Management.
* **Abdelrahman Shehata**: Implementation of Round Robin Scheduling Algorithm.
* **Melik Magdy**: Implementation of Priority Scheduling Algorithm. 
* **Ahmed Atia**: JavaFX GUI Layout & User Interface Design.
* **Kaream Walied**: Gantt Chart Visualization Logic.
* **Mahmoud Nasser**: Input validation & (MVC) Implementation.
* **Raghad Mohamed**: Testing & run.sh Implementation.
* **Abdelrahman Awad**: Documentation & Building Scenarios logic. 

---

##  Project Description

This project implements and compares:

- Round Robin 
- Priority Scheduling

Both algorithms are executed on the same workload and compared using:

- Waiting Time (WT)
- Turnaround Time (TAT)
- Response Time (RT)
- Average WT, TAT, RT

---

##  Priority Rule

Lower priority value = Higher priority.

Priority Scheduling is implemented as **Preemptive**.

### Tie-breaking rule:
- If processes have equal priority, the earlier arrival runs first.
- If they also share the same arrival time, they are handled in Round Robin order using the time quantum.

---

##  Features

 Dynamic process input  
 Case-insensitive duplicate ID prevention  
 Full input validation  
 Separate Gantt charts  
 Metrics calculation  
 Comparison summary  
 Starvation discussion  
 JavaFX GUI  

---

##  Project Structure

src/
├── model/
├── scheduler/
├── gui/
test-cases/
screenshots/
README.md
run.sh

---

##  How to Compile (Linux)

javac -d bin
--module-path /usr/share/openjfx/lib
--add-modules javafx.controls
src/model/.java src/scheduler/.java src/gui/MainApp.java

---

##  How to Run (Linux)

java -cp bin
--module-path /usr/share/openjfx/lib
--add-modules javafx.controls gui.MainApp

or using the run button that already has the paths 
---by using ./run.sh   command 

-- in both cases the paths must be adjusted according the to new path in the machine where the project will run 

Running from an IDE
--If using an IDE such as VS Code or IntelliJ:
--Open MainApp.java
--Use the Run or Run & Debug option
--This requires JavaFX to be properly configured in the IDE.

---

##  Java Version

Java 17  
JavaFX (OpenJFX) 

---

##  Test Scenarios

Five different workload scenarios are included in the test-cases/ folder:
- Basic mixed workload
- Urgency-focused case
- Fairness case
- Starvation-sensitive case
- Input Validation case

---

##  Comparison Conclusion

- Round Robin improves fairness and response time.
- Priority Scheduling favors urgent processes.
- Starvation risk exists in Priority Scheduling.

