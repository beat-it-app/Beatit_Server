package com.beat_it.team.entity

import jakarta.persistence.*

@Entity
@Table(
    name = "team_cloud_folders",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_team_folder_name",
            columnNames = ["team_id", "folder_name"]
        )
    ]
)
class TeamCloudFolders(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_cloud_folder_id")
    val teamCloudFolderId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    val team: Teams,

    @Column(name = "folder_name", nullable = false)
    var folderName: String,

    @Column(name = "creator_id", nullable = false)
    val creatorId: Long,

    @OneToMany(mappedBy = "teamCloudFolder", cascade = [CascadeType.ALL], orphanRemoval = true)
    val items: MutableList<TeamCloudItems> = mutableListOf()
){
    fun updateFolderName(folderName: String) {
        this.folderName = folderName
    }
}