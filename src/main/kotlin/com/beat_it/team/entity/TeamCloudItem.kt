package com.beat_it.team.entity

import com.beat_it.global.entity.BaseCreatedTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "team_cloud_items")
class TeamCloudItems(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_cloud_item_id")
    val teamCloudItemId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    val team: Teams,

    @Column(name = "uploader_id", nullable = false)
    val uploaderId: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_cloud_folder_id", nullable = true)
    var teamCloudFolder: TeamCloudFolders? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_file_id", nullable = true)
    val teamFile: TeamFile? = null,

    @Column(name = "link_url", length = 1000)
    val linkUrl: String? = null,

    @Column(name = "item_name", nullable = false)
    val itemName: String
) : BaseCreatedTimeEntity() {

    fun changeFolder(folder: TeamCloudFolders?) {
        this.teamCloudFolder = folder
    }
}