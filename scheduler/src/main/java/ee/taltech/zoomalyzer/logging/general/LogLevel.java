package ee.taltech.zoomalyzer.logging.general;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class LogLevel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    public LogLevel() {
    }

    public LogLevel(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String level) {
        this.name = level;
    }

    @Override
    public String toString() {
        return "LogLevel{" +
                "name='" + name + '\'' +
                '}';
    }
}
